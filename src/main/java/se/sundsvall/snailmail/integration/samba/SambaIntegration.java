package se.sundsvall.snailmail.integration.samba;

import static java.util.Optional.ofNullable;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import se.sundsvall.snailmail.api.model.EnvelopeType;
import se.sundsvall.snailmail.integration.db.model.AttachmentEntity;
import se.sundsvall.snailmail.integration.db.model.BatchEntity;
import se.sundsvall.snailmail.integration.db.model.DepartmentEntity;
import se.sundsvall.snailmail.integration.db.model.RequestEntity;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.transaction.Transactional;
import jcifs.CIFSContext;
import jcifs.context.SingletonContext;
import jcifs.smb.NtlmPasswordAuthenticator;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;

@Component
@CircuitBreaker(name = "sambaIntegration")
@EnableConfigurationProperties(SambaIntegrationProperties.class)
public class SambaIntegration {

	private static final Logger LOGGER = LoggerFactory.getLogger(SambaIntegration.class);

	private static final String FILE_PREFIX = "sandlista-";
	private static final String CSV_HEADER = "namn,careOf,adress,lagenhet,postnummer,postort";
	private static final String CSV_FORMAT = "%s,%s,%s,%s,%s,%s%n";

	private final CIFSContext context;

	private final String shareUrl;

	public SambaIntegration(final SambaIntegrationProperties properties) {
		// Initialize the JCIFS context
		context = SingletonContext.getInstance()
			.withCredentials(new NtlmPasswordAuthenticator(properties.domain(), properties.username(), properties.password()));

		shareUrl = String.format("smb://%s:%d%s", properties.host(), properties.port(), properties.share());
	}

	@Transactional
	public void writeBatchDataToSambaShare(final BatchEntity batchEntity) {
		// HashMap<batchPath, filename>. where files should be saved.
		HashMap<String, String> batchPathFileNameMap = new HashMap<>();

		// HashMap<path, csv content list>. Where the csv content should be saved.
		HashMap<String, List<String>> batchPathCsvMap = new HashMap<>();

		// Create the department and batch folders
		createDepartmentAndBatchFolders(batchEntity);

		// Save all attachments where they belong
		saveAttachments(batchEntity, batchPathFileNameMap);

		// Add csv content
		createCsvContent(batchEntity, batchPathCsvMap);

		//Now we save each csv file where it belongs
		saveCsvContent(batchPathCsvMap, batchPathFileNameMap);
	}

	/**
	 * Save all attachments where they belong
	 *
	 * @param batchEntity the batch entity
	 * @param fileNameMap the map with the filename for each batchPath
	 */
	private void saveAttachments(BatchEntity batchEntity, HashMap<String, String> fileNameMap) {
		batchEntity.getDepartmentEntities().forEach(
			department -> {
				var batchPath = getBatchPath(department, batchEntity);
				department.getRequestEntities()
					.forEach(request -> {
						LOGGER.info("Saving attachment for request");
						fileNameMap.put(batchPath, findFile(request, batchPath));
						request.getAttachmentEntities().forEach(attachmentEntity -> saveAttachment(attachmentEntity, batchPath));
					});
			}
		);
	}

	/**
	 * Store where csv content should be created (batchPath)
	 *
	 * @param departmentBatchPathMap the map with the csv content for each department and batch
	 * @param fileNameMap the map with the filename for each batchPath
	 */
	private void saveCsvContent(HashMap<String, List<String>> departmentBatchPathMap, HashMap<String, String> fileNameMap) {
		LOGGER.info("Starting to write csv content to Samba server");
		departmentBatchPathMap.keySet().forEach(departmentPath -> {
			//For each key in the map, create a new file with the content
			try {
				//Get the filename from the map.
				var fileName = fileNameMap.get(departmentPath);
				var destinationFile = new SmbFile(fileName, context);

				if (!destinationFile.exists()) {
					destinationFile.createNewFile();
				}

				try (var smbFileOutputStream = new SmbFileOutputStream(destinationFile, true); // append mode
				     var printWriter = new PrintWriter(smbFileOutputStream)) {
					// Write the headers to the file only if it's a new file
					LOGGER.info("Writing headers to file: {}", destinationFile);
					printWriter.println(CSV_HEADER);
					LOGGER.info("Writing address(es) to file: {}", destinationFile);
					departmentBatchPathMap.get(departmentPath).forEach(printWriter::print);
				}
			} catch (final IOException e) {
				throw Problem.valueOf(Status.INTERNAL_SERVER_ERROR, "Failed to write to Samba share");
			}
		});
	}

	/**
	 * Create the csv content for each batch
	 *
	 * @param batchEntity the batch entity where everything should be stored
	 * @param departmentBatchPathMap the map to be populated with the csv content for each department and batch
	 */
	private void createCsvContent(BatchEntity batchEntity, HashMap<String, List<String>> departmentBatchPathMap) {
		batchEntity.getDepartmentEntities().forEach(
			department -> {
				var batchPath = getBatchPath(department, batchEntity);

				department.getRequestEntities()
					.forEach(request -> {
						// Only save the request data if it's not a windowed envelope
						if (!EnvelopeType.WINDOWED.equals(request.getAttachmentEntities().getFirst().getEnvelopeType())) {
							//If the key already exists, append the content

							if (departmentBatchPathMap.containsKey(batchPath)) {
								LOGGER.info("Appending csv information to existing csv content");
								departmentBatchPathMap.get(batchPath).add(createCsvRow(request));
							} else {
								//If it doesn't exist, create a new list with the content
								LOGGER.info("Creating new csv content");

								List<String> csvContent = new ArrayList<>();
								csvContent.add(createCsvRow(request));
								departmentBatchPathMap.put(batchPath, csvContent);
							}
						}
					});
			}
		);
	}

	/**
	 * Create a row for the csv
	 *
	 * @param request the request entity
	 * @return the row that should be saved in the csv file
	 */
	private String createCsvRow(final RequestEntity request) {
		LOGGER.info("Creating csv content");
		var stringWriter = new StringWriter();
		var printWriter = new PrintWriter(stringWriter);
		var recipient = request.getRecipientEntity();

		var name = recipient.getGivenName() + " " + recipient.getLastName();
		var careOf = recipient.getCareOf();
		var address = recipient.getAddress();
		var apartmentNumber = recipient.getApartmentNumber();
		var postalCode = recipient.getPostalCode();
		var city = recipient.getCity();

		printWriter.printf(CSV_FORMAT, name, careOf, address, apartmentNumber, postalCode, city);
		return stringWriter.toString();
	}

	private void createDepartmentAndBatchFolders(BatchEntity batchEntity) {
		LOGGER.info("Creating department and batch folders");
		batchEntity.getDepartmentEntities().forEach(
			department -> {
				var departmentPath = getDepartmentPath(department);
				createFolder(departmentPath);

				var batchPath = getBatchPath(department, batchEntity);
				createFolder(batchPath);
			}
		);

	}

	private String getDepartmentPath(DepartmentEntity departmentEntity) {
		return shareUrl + departmentEntity.getName();
	}

	private String getBatchPath(DepartmentEntity departmentEntity, BatchEntity batchEntity) {
		return ofNullable(batchEntity.getIssuer())
			.map(issuer -> getDepartmentPath(departmentEntity) + File.separator + issuer + "_" + batchEntity.getId())
			.orElse(getDepartmentPath(departmentEntity) + File.separator + batchEntity.getId());
	}

	private void saveAttachment(final AttachmentEntity attachmentEntity, final String departmentPath) {
		var attachmentFile = departmentPath + File.separator + attachmentEntity.getName();

		try (var destinationAttachmentFile = new SmbFile(attachmentFile, context)) {
			if (!destinationAttachmentFile.exists()) {
				destinationAttachmentFile.createNewFile();

				try (var smbFileOutputStream = new SmbFileOutputStream(destinationAttachmentFile)) {
					LOGGER.info("Writing file: {}", destinationAttachmentFile.getName());
					smbFileOutputStream.write(Base64.getDecoder().decode(attachmentEntity.getContent()));
				}
			}
		} catch (final IOException e) {
			throw Problem.valueOf(Status.INTERNAL_SERVER_ERROR, "Failed to write attachmentEntity to Samba share");
		}
	}

	private String findFile(final RequestEntity requestEntity, final String departmentPath) {
		return ofNullable(requestEntity.getAttachmentEntities().getFirst().getName())
			.map(name -> name.substring(0, Optional.of(name.lastIndexOf("."))
				.filter(i -> i != -1)
				.orElse(name.length())))
			.map(name -> departmentPath + File.separator + FILE_PREFIX + name + ".csv")
			.orElseThrow(() -> Problem.valueOf(Status.INTERNAL_SERVER_ERROR, "AttachmentEntity name is null"));
	}

	private void createFolder(final String folder) {
		try (var folderFile = new SmbFile(folder, context)) {
			if (!folderFile.exists()) {
				LOGGER.info("Folder: {}, doesn't exist, creating it.", folderFile);
				folderFile.mkdir();
			} else {
				LOGGER.info("Folder: {}, exits, not creating it", folderFile);
			}
		} catch (final SmbException | MalformedURLException e) {
			throw Problem.valueOf(Status.INTERNAL_SERVER_ERROR, "Failed to create folder " + folder + " on Samba share");
		}
	}

	// For testing purposes
	CIFSContext getContext() {
		return context;
	}

}
