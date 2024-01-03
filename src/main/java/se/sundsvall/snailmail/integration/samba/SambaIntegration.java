package se.sundsvall.snailmail.integration.samba;

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
import se.sundsvall.snailmail.integration.db.model.Attachment;
import se.sundsvall.snailmail.integration.db.model.Batch;
import se.sundsvall.snailmail.integration.db.model.Request;

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

	public void writeBatchDataToSambaShare(final Batch batch) {

		// Create the batch folder
		final var batchPath = shareUrl + batch.getId();
		createFolder(batchPath);

		batch.getDepartments().forEach(
			department -> {
				// Create the department folders
				final var departmentPath = batchPath + File.separator + department.getName();
				createFolder(departmentPath);
				// Save the data to the files
				department.getRequests()
					.forEach(request -> {
						final var attachment = request.getAttachments().getFirst();
						saveAttachment(attachment, departmentPath);

						// Only save the request data if it's not a windowed envelope
						if (!attachment.getEnvelopeType().equals(EnvelopeType.WINDOWED)) {
							saveRequestDataToFile(request, departmentPath);
						}
					});
			}
		);
	}

	private void saveRequestDataToFile(final Request request, final String departmentPath) {

		final var targetFile = findFile(request, departmentPath);

		try (final var destinationFile = new SmbFile(targetFile, context)) {
			if (!destinationFile.exists()) {
				destinationFile.createNewFile();
			}

			try (final var smbFileOutputStream = new SmbFileOutputStream(destinationFile, true); // append mode
			     final var printWriter = new PrintWriter(smbFileOutputStream)) {

				// Write the headers to the file only if it's a new file
				if (destinationFile.length() == 0) {
					printWriter.println("namn,careOf,adress,postnummer,postort");
				}

				final var name = request.getRecipient().getGivenName() + " " + request.getRecipient().getLastName();
				final var careOf = request.getRecipient().getCo();
				final var address = request.getRecipient().getAdress();
				final var postalCode = request.getRecipient().getPostalCode();
				final var city = request.getRecipient().getCity();

				printWriter.printf("%s,%s,%s,%s,%s%n", name, careOf, address, postalCode, city);
			}
		} catch (final IOException e) {
			throw Problem.valueOf(Status.INTERNAL_SERVER_ERROR, "Failed to write  to Samba share");
		}
	}

	private void saveAttachment(final Attachment attachment, final String departmentPath) {
		final var attachmentFile = departmentPath + File.separator + attachment.getName();

		try (final var destinationAttachmentFile = new SmbFile(attachmentFile, context)) {
			if (!destinationAttachmentFile.exists()) {
				destinationAttachmentFile.createNewFile();

				try (final var smbFileOutputStream = new SmbFileOutputStream(destinationAttachmentFile)) {

					smbFileOutputStream.write(Base64.getDecoder().decode(attachment.getContent()));
				}
			}
		} catch (final IOException e) {
			throw Problem.valueOf(Status.INTERNAL_SERVER_ERROR, "Failed to write attachment to Samba share");
		}
	}


	String findFile(final Request request, final String departmentPath) {
		return Optional.ofNullable(request.getAttachments().getFirst().getName())
			.map(name -> name.substring(0, Optional.of(name.lastIndexOf("."))
				.filter(i -> i != -1)
				.orElse(name.length())))
			.map(name -> departmentPath + File.separator + FILE_PREFIX + name + ".csv")
			.orElseThrow(() -> Problem.valueOf(Status.INTERNAL_SERVER_ERROR, "Attachment name is null"));
	}

	void createFolder(final String folder) {
		try (final var folderFile = new SmbFile(folder, context)) {
			if (!folderFile.exists()) {
				LOGGER.info("Folder: {}, doesn't exist, creating it.", folderFile);
				folderFile.mkdir();
			}
		} catch (final SmbException | MalformedURLException e) {
			throw Problem.valueOf(Status.INTERNAL_SERVER_ERROR, "Failed to create folder " + folder + " on Samba share");
		}
	}

}
