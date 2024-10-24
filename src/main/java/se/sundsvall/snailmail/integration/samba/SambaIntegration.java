package se.sundsvall.snailmail.integration.samba;

import static java.util.Optional.ofNullable;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.Base64;
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
import se.sundsvall.snailmail.integration.db.model.RequestEntity;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
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

	private static final String FILE_PREFIX = "sandlista-";

	private static final Logger LOGGER = LoggerFactory.getLogger(SambaIntegration.class);

	private final CIFSContext context;

	private final String shareUrl;

	public SambaIntegration(final SambaIntegrationProperties properties) {
		// Initialize the JCIFS context
		context = SingletonContext.getInstance()
			.withCredentials(new NtlmPasswordAuthenticator(properties.domain(), properties.username(), properties.password()));

		shareUrl = String.format("smb://%s:%d%s", properties.host(), properties.port(), properties.share());
	}

	public void writeBatchDataToSambaShare(final BatchEntity batchEntity) {

		batchEntity.getDepartmentEntities().forEach(
			department -> {
				// Create the department folders
				var departmentPath = shareUrl + department.getName();
				createFolder(departmentPath);

				// Create the batchEntity folder
				var batchPath = ofNullable(batchEntity.getIssuer())
					.map(issuer -> departmentPath + File.separator + issuer + "_" + batchEntity.getId())
					.orElse(departmentPath + File.separator + batchEntity.getId());

				createFolder(batchPath);

				// Save the data to the files
				department.getRequestEntities()
					.forEach(request -> {
						LOGGER.info("Saving attachment for request");
						request.getAttachmentEntities().forEach(attachmentEntity -> saveAttachment(attachmentEntity, batchPath));
						// Only save the request data if it's not a windowed envelope
						if (!EnvelopeType.WINDOWED.equals(request.getAttachmentEntities().getFirst().getEnvelopeType())) {
							saveRequestDataToFile(request, batchPath);
						}
					});
			}
		);
	}

	private void saveRequestDataToFile(final RequestEntity request, final String departmentPath) {
		var targetFile = findFile(request, departmentPath);

		try (var destinationFile = new SmbFile(targetFile, context)) {
			if (!destinationFile.exists()) {
				destinationFile.createNewFile();
			}

			try (var smbFileOutputStream = new SmbFileOutputStream(destinationFile, true); // append mode
			     var printWriter = new PrintWriter(smbFileOutputStream)) {

				// Write the headers to the file only if it's a new file
				if (destinationFile.length() == 0) {
					printWriter.println("namn,careOf,adress,lagenhet,postnummer,postort");
					LOGGER.info("new file writing headers");
				}

				var recipient = request.getRecipientEntity();

				var name = recipient.getGivenName() + " " + recipient.getLastName();
				var careOf = recipient.getCareOf();
				var address = recipient.getAddress();
				var apartmentNumber = recipient.getApartmentNumber();
				var postalCode = recipient.getPostalCode();
				var city = recipient.getCity();

				printWriter.printf("%s,%s,%s,%s,%s,%s%n", name, careOf, address, apartmentNumber, postalCode, city);
			}
		} catch (final IOException e) {
			throw Problem.valueOf(Status.INTERNAL_SERVER_ERROR, "Failed to write  to Samba share");
		}
	}

	private void saveAttachment(final AttachmentEntity attachmentEntity, final String departmentPath) {
		var attachmentFile = departmentPath + File.separator + attachmentEntity.getName();

		try (var destinationAttachmentFile = new SmbFile(attachmentFile, context)) {
			if (!destinationAttachmentFile.exists()) {
				destinationAttachmentFile.createNewFile();

				try (var smbFileOutputStream = new SmbFileOutputStream(destinationAttachmentFile)) {
					LOGGER.info("Writing file");
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
