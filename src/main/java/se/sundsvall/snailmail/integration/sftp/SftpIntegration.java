package se.sundsvall.snailmail.integration.sftp;

import static java.util.Optional.ofNullable;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.sftp.session.SftpSession;
import org.springframework.stereotype.Component;
import org.zalando.problem.Problem;
import se.sundsvall.snailmail.api.model.EnvelopeType;
import se.sundsvall.snailmail.integration.db.model.AttachmentEntity;
import se.sundsvall.snailmail.integration.db.model.BatchEntity;
import se.sundsvall.snailmail.integration.db.model.DepartmentEntity;
import se.sundsvall.snailmail.integration.db.model.RequestEntity;

@Component
@CircuitBreaker(name = "sftpIntegration")
public class SftpIntegration {

	private static final Logger LOGGER = LoggerFactory.getLogger(SftpIntegration.class);

	private static final String FILE_PREFIX = "sandlista-";
	private static final String CSV_HEADER = "namn,careOf,adress,lagenhet,postnummer,postort";
	private static final String CSV_FORMAT = "%s,%s,%s,%s,%s,%s%n";

	private final SftpProperties sftpProperties;
	private final DefaultSftpSessionFactory sftpSessionFactory;
	private SftpSession sftpSession;

	public SftpIntegration(final DefaultSftpSessionFactory sftpSessionFactory, final SftpProperties sftpProperties) {
		this.sftpSessionFactory = sftpSessionFactory;
		this.sftpProperties = sftpProperties;
	}

	@Transactional
	public void writeBatchDataToSftp(final BatchEntity batchEntity) {

		this.sftpSession = sftpSessionFactory.getSession();
		// HashMap<batchPath, filename>. where files should be saved.
		Map<String, String> batchPathFileNameMap = new HashMap<>();

		// HashMap<path, csv content list>. Where the csv content should be saved.
		Map<String, List<String>> batchPathCsvMap = new HashMap<>();

		// Create the department and batch folders
		createDepartmentAndBatchFolders(batchEntity);

		// Save all attachments where they belong
		saveAttachments(batchEntity, batchPathFileNameMap);

		// Add csv content
		createCsvContent(batchEntity, batchPathCsvMap);

		// Now we save each csv file where it belongs
		saveCsvContent(batchPathCsvMap, batchPathFileNameMap);
	}

	private void createDepartmentAndBatchFolders(final BatchEntity batchEntity) {
		LOGGER.info("Creating department and batch folders");
		batchEntity.getDepartmentEntities().forEach(
			department -> {
				var departmentPath = getDepartmentPath(department);
				createFolder(departmentPath);

				var batchPath = getBatchPath(department, batchEntity);
				createFolder(batchPath);
			});

	}

	private String getBatchPath(final DepartmentEntity departmentEntity, final BatchEntity batchEntity) {
		return ofNullable(batchEntity.getSentBy())
			.map(sentBy -> getDepartmentPath(departmentEntity) + File.separator + sentBy + "_" + batchEntity.getId())
			.orElse(getDepartmentPath(departmentEntity) + File.separator + batchEntity.getId());
	}

	private String getDepartmentPath(final DepartmentEntity departmentEntity) {
		if (departmentEntity.getFolderName() == null || departmentEntity.getFolderName().isBlank()) {
			return sftpProperties.defaultPath() + departmentEntity.getName();
		}
		return sftpProperties.defaultPath() + departmentEntity.getFolderName() + File.separator + departmentEntity.getName();
	}

	private void createFolder(final String folder) {
		try {
			if (!sftpSession.exists(folder)) {
				LOGGER.info("Folder: {}, doesn't exist, creating it.", folder);
				sftpSession.mkdir(folder);
			} else {
				LOGGER.info("Folder: {}, exits, not creating it", folder);
			}
		} catch (Exception e) {
			LOGGER.error("Failed to create folder {} on SFTP server", folder, e);
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Failed to create folder " + folder + " on SFTP server");
		}
	}

	private void saveAttachments(final BatchEntity batchEntity, final Map<String, String> fileNameMap) {
		batchEntity.getDepartmentEntities().forEach(
			department -> {
				var batchPath = getBatchPath(department, batchEntity);
				department.getRequestEntities()
					.forEach(request -> {
						LOGGER.info("Saving attachment for request");
						fileNameMap.put(batchPath, findFile(request, batchPath));
						request.getAttachmentEntities().forEach(attachmentEntity -> saveAttachment(attachmentEntity, batchPath));
					});
			});
	}

	private String findFile(final RequestEntity requestEntity, final String departmentPath) {
		return ofNullable(requestEntity.getAttachmentEntities().getFirst().getName())
			.map(name -> name.substring(0, Optional.of(name.lastIndexOf("."))
				.filter(i -> i != -1)
				.orElse(name.length())))
			.map(name -> departmentPath + File.separator + FILE_PREFIX + name + ".csv")
			.orElseThrow(() -> Problem.valueOf(INTERNAL_SERVER_ERROR, "Attachment name is null"));
	}

	private void saveAttachment(final AttachmentEntity attachmentEntity, final String departmentPath) {
		var attachmentFile = departmentPath + File.separator + attachmentEntity.getName();
		try {
			LOGGER.info("Saving attachment {} on SFTP server", attachmentFile);
			sftpSession.write(new ByteArrayInputStream(attachmentEntity.getContent().getBytes()), attachmentFile);
		} catch (Exception e) {
			LOGGER.error("Failed to save attachment {} on SFTP server", attachmentFile, e);
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Failed to save attachment " + attachmentFile + " on SFTP server");
		}
	}

	private void createCsvContent(final BatchEntity batchEntity, final Map<String, List<String>> departmentBatchPathMap) {
		batchEntity.getDepartmentEntities().forEach(department -> {
			var batchPath = getBatchPath(department, batchEntity);

			department.getRequestEntities().forEach(request -> {
				// Only save the request data if it's not a windowed envelope
				if (!EnvelopeType.WINDOWED.equals(request.getAttachmentEntities().getFirst().getEnvelopeType())) {

					if (departmentBatchPathMap.containsKey(batchPath)) {
						// If the key already exists, append the content to the existing list
						LOGGER.info("Appending csv information to existing csv content");

						departmentBatchPathMap.get(batchPath).add(createCsvRow(request));
					} else {
						// If it doesn't exist, create a new list with the content
						LOGGER.info("Creating new csv content");

						List<String> csvContent = new ArrayList<>();
						csvContent.add(createCsvRow(request));
						departmentBatchPathMap.put(batchPath, csvContent);
					}
				}
			});
		});
	}

	/**
	 * Create a row for the csv
	 *
	 * @param  request the request entity
	 * @return         the row that should be saved in the csv file
	 */
	private String createCsvRow(final RequestEntity request) {
		LOGGER.info("Creating csv content");
		var stringWriter = new StringWriter();
		var printWriter = new PrintWriter(stringWriter);
		var recipient = request.getRecipientEntity();

		var name = recipient.getGivenName() + " " + recipient.getLastName();
		var address = recipient.getAddress();
		var postalCode = recipient.getPostalCode();
		var city = recipient.getCity();
		var careOf = ofNullable(recipient.getCareOf()).orElse(""); // If careOf is null, set it to empty string
		var apartmentNumber = ofNullable(recipient.getApartmentNumber()).orElse(""); // If apartmentNumber is null, set it to empty string

		printWriter.printf(CSV_FORMAT, name, careOf, address, apartmentNumber, postalCode, city);
		return stringWriter.toString();
	}

	/**
	 * Store the csv content on the samba server
	 *
	 * @param departmentBatchPathMap the map with the csv content for each department and batch
	 * @param fileNameMap            the map with the filename for each batchPath
	 */
	private void saveCsvContent(final Map<String, List<String>> departmentBatchPathMap, final Map<String, String> fileNameMap) {
		LOGGER.info("Starting to write csv content to SFTP server");
		departmentBatchPathMap.forEach((departmentPath, contentList) -> {
			try {
				var remoteFilePath = fileNameMap.get(departmentPath);
				var remoteDirectory = remoteFilePath.substring(0, remoteFilePath.lastIndexOf('/'));

				if (!sftpSession.exists(remoteDirectory)) {
					sftpSession.mkdir(remoteDirectory);
					LOGGER.debug("Created directory: {}", remoteDirectory);
				}

				boolean isNewFile = !sftpSession.exists(remoteFilePath);
				var existingContent = readExistingContent(remoteFilePath, isNewFile);

				try (var byteArrayOutputStream = new ByteArrayOutputStream();
					var writer = new OutputStreamWriter(byteArrayOutputStream, StandardCharsets.ISO_8859_1);
					var printWriter = new PrintWriter(writer, false)) {

					if (isNewFile) {
						printWriter.println(CSV_HEADER);
					} else if (!existingContent.isEmpty()) {
						printWriter.print(existingContent);
					}

					contentList.forEach(printWriter::print);

					printWriter.flush();

					sftpSession.write(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()), remoteFilePath);
					LOGGER.info("Successfully wrote CSV data to: {}", remoteFilePath);
				}
			} catch (IOException e) {
				LOGGER.error("Failed to write to SFTP server", e);
				throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Failed to write to SFTP server");
			}
		});
	}

	/**
	 * Read the existing content of the file
	 *
	 * @param  remoteFilePath the path to the file
	 * @param  isNewFile      if the file is new
	 * @return                the existing content of the file
	 */
	private String readExistingContent(final String remoteFilePath, boolean isNewFile) {
		if (isNewFile) {
			return "";
		}
		try (var outputStream = new ByteArrayOutputStream()) {
			sftpSession.read(remoteFilePath, outputStream);
			return outputStream.toString(StandardCharsets.ISO_8859_1);
		} catch (IOException e) {
			LOGGER.warn("Failed to read existing file: {}. It may be empty.", remoteFilePath, e);
			return "";
		}
	}
}
