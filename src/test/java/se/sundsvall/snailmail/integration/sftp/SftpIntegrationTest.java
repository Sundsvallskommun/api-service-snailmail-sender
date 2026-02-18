package se.sundsvall.snailmail.integration.sftp;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import se.sundsvall.snailmail.Application;
import se.sundsvall.snailmail.api.model.EnvelopeType;
import se.sundsvall.snailmail.integration.db.model.AttachmentEntity;
import se.sundsvall.snailmail.integration.db.model.BatchEntity;
import se.sundsvall.snailmail.integration.db.model.DepartmentEntity;
import se.sundsvall.snailmail.integration.db.model.RecipientEntity;
import se.sundsvall.snailmail.integration.db.model.RequestEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static se.sundsvall.snailmail.api.model.EnvelopeType.PLAIN;
import static se.sundsvall.snailmail.api.model.EnvelopeType.WINDOWED;

@ActiveProfiles("junit")
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@Testcontainers
class SftpIntegrationTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(SftpIntegrationTest.class);
	private static final String SFTP_SEPARATOR = "/";

	@Container
	private static GenericContainer<?> sftpContainer = new GenericContainer<>("atmoz/sftp:latest")
		.withExposedPorts(22)
		.withCommand("user:password:1001::upload");

	@DynamicPropertySource
	static void afterSftpContainerStarted(final DynamicPropertyRegistry registry) {
		registry.add("integration.sftp.host", sftpContainer::getHost);
		registry.add("integration.sftp.port", () -> sftpContainer.getMappedPort(22));
		registry.add("integration.sftp.username", () -> "user");
		registry.add("integration.sftp.password", () -> "password");
		registry.add("integration.sftp.allowUnknownKeys", () -> "true");
	}

	@Autowired
	private DefaultSftpSessionFactory sftpSessionFactory;

	@Autowired
	private SftpIntegration sftpIntegration;

	@Test
	void testWriteBatchDataToSftp_PLAIN() throws Exception {
		final var sftpSession = sftpSessionFactory.getSession();
		final var batchEntity = createBatchEntity("attachment2.pdf", PLAIN);

		sftpIntegration.writeBatchDataToSftp(batchEntity);

		final var expectedCsvFile = "/upload/department1" + SFTP_SEPARATOR + "joe01doe_123" + SFTP_SEPARATOR + "sandlista-attachment2.csv";
		assertThat(sftpSession.exists(expectedCsvFile)).isTrue();

		final var expectedPdf = "/upload/department1" + SFTP_SEPARATOR + "joe01doe_123" + SFTP_SEPARATOR + "attachment2.pdf";
		assertThat(sftpSession.exists(expectedPdf)).isTrue();

		final var baos = new ByteArrayOutputStream();
		sftpSession.read(expectedCsvFile, baos);
		final var csvContent = baos.toString(StandardCharsets.ISO_8859_1);
		assertThat(csvContent)
			.contains("namn,careOf,adress,lagenhet,postnummer,postort")// CSV header
			.contains("John Doe,Bruce Wayne,123 Street,1001,12345,Gotham City"); // CSV rad
		baos.reset();

		sftpSession.read(expectedPdf, baos);
		final var pdfContent = baos.toString(StandardCharsets.ISO_8859_1);
		assertThat(pdfContent)
			.contains("base64");

		LOGGER.info("SFTP csv content: \n{}", csvContent);
		LOGGER.info("SFTP pdf content: \n{}", pdfContent);
		sftpSession.close();
	}

	@Test
	void testWriteBatchDataToSftp_WINDOWED() throws Exception {
		final var sftpSession = sftpSessionFactory.getSession();
		final var batchEntity = createBatchEntity("attachment123.pdf", WINDOWED);

		sftpIntegration.writeBatchDataToSftp(batchEntity);

		final var expectedCsvFile = "/upload/department1" + SFTP_SEPARATOR + "joe01doe_123" + SFTP_SEPARATOR + "sandlista-attachment123.csv";
		assertThat(sftpSession.exists(expectedCsvFile)).isFalse(); // Test that the CSV file is not created for windowed envelopes

		final var expectedPdf = "/upload/department1" + SFTP_SEPARATOR + "joe01doe_123" + SFTP_SEPARATOR + "attachment123.pdf";
		assertThat(sftpSession.exists(expectedPdf)).isTrue();

		final var baos = new ByteArrayOutputStream();
		sftpSession.read(expectedPdf, baos);
		final var pdfContent = baos.toString(StandardCharsets.ISO_8859_1);
		assertThat(pdfContent)
			.contains("base64");

		LOGGER.info("SFTP pdf content: \n{}", pdfContent);
		sftpSession.close();
	}

	@Test
	void testAppendToExistingCsvFile() throws Exception {
		final var sftpSession = sftpSessionFactory.getSession();
		final var batchEntity = createBatchEntity("attachment1234.pdf", PLAIN);

		// Writing initial data to SFTP
		sftpIntegration.writeBatchDataToSftp(batchEntity);

		final var expectedCsvFile = "/upload/department1" + SFTP_SEPARATOR + "joe01doe_123" + SFTP_SEPARATOR + "sandlista-attachment1234.csv";
		assertThat(sftpSession.exists(expectedCsvFile)).isTrue();

		final var outputStream = new ByteArrayOutputStream();
		sftpSession.read(expectedCsvFile, outputStream);
		final String initialFileContent = outputStream.toString(StandardCharsets.ISO_8859_1);
		outputStream.reset();

		final var batchEntity2 = createBatchEntity("attachment1234.pdf", PLAIN);
		sftpIntegration.writeBatchDataToSftp(batchEntity2);

		sftpSession.read(expectedCsvFile, outputStream);
		final var updatedFileContent = outputStream.toString(StandardCharsets.ISO_8859_1);
		outputStream.reset();

		assertThat(updatedFileContent.length()).isGreaterThan(initialFileContent.length());
		assertThat(updatedFileContent).contains("John Doe,Bruce Wayne,123 Street,1001,12345,Gotham City");
		assertThat(updatedFileContent.split("\n")).hasSizeGreaterThan(initialFileContent.split("\n").length);

		final var expectedPdfFile = "/upload/department1" + SFTP_SEPARATOR + "joe01doe_123" + SFTP_SEPARATOR + "attachment1234.pdf";
		sftpSession.read(expectedPdfFile, outputStream);
		final var pdfContent = outputStream.toString(StandardCharsets.ISO_8859_1);
		assertThat(pdfContent).contains("base64");

		LOGGER.info("Initial SFTP file content:\n{}", initialFileContent);
		LOGGER.info("Updated SFTP file content:\n{}", updatedFileContent);
		sftpSession.close();
	}

	// Very similar to the first test but with folder name set in department entity
	@Test
	void testWriteBatchDataWithFolderToSftp() throws Exception {
		final var sftpSession = sftpSessionFactory.getSession();
		final var batchEntity = createBatchEntity("attachment2.pdf", PLAIN);
		batchEntity.getDepartmentEntities().getFirst().setFolderName("folder1");

		sftpIntegration.writeBatchDataToSftp(batchEntity);

		final var expectedCsvFile = "/upload/folder1/department1" + SFTP_SEPARATOR + "joe01doe_123" + SFTP_SEPARATOR + "sandlista-attachment2.csv";
		assertThat(sftpSession.exists(expectedCsvFile)).isTrue();

		final var expectedPdf = "/upload/folder1/department1" + SFTP_SEPARATOR + "joe01doe_123" + SFTP_SEPARATOR + "attachment2.pdf";
		assertThat(sftpSession.exists(expectedPdf)).isTrue();

		final var baos = new ByteArrayOutputStream();
		sftpSession.read(expectedCsvFile, baos);
		final var csvContent = baos.toString(StandardCharsets.ISO_8859_1);
		assertThat(csvContent)
			.contains("namn,careOf,adress,lagenhet,postnummer,postort")// CSV header
			.contains("John Doe,Bruce Wayne,123 Street,1001,12345,Gotham City"); // CSV rad
		baos.reset();

		sftpSession.read(expectedPdf, baos);
		final var pdfContent = baos.toString(StandardCharsets.ISO_8859_1);
		assertThat(pdfContent)
			.contains("base64");

		LOGGER.info("SFTP csv content: \n{}", csvContent);
		LOGGER.info("SFTP pdf content: \n{}", pdfContent);
		sftpSession.close();
	}

	private BatchEntity createBatchEntity(final String fileName, final EnvelopeType envelopeType) {
		final var recipient = new RecipientEntity();
		recipient.setGivenName("John");
		recipient.setLastName("Doe");
		recipient.setCareOf("Bruce Wayne");
		recipient.setApartmentNumber("1001");
		recipient.setAddress("123 Street");
		recipient.setPostalCode("12345");
		recipient.setCity("Gotham City");

		final var attachmentEntity = new AttachmentEntity();
		attachmentEntity.setName(fileName);
		attachmentEntity.setContent("base64");
		attachmentEntity.setContentType("application/pdf");
		attachmentEntity.setEnvelopeType(envelopeType);

		final var requestEntity = new RequestEntity();
		requestEntity.setRecipientEntity(recipient);
		requestEntity.setAttachmentEntities(List.of(attachmentEntity));

		final var department = new DepartmentEntity();
		department.setName("department1");
		department.setRequestEntities(List.of(requestEntity));

		final var batchEntity = new BatchEntity();
		batchEntity.setId("123");
		batchEntity.setSentBy("joe01doe");
		batchEntity.setDepartmentEntities(List.of(department));

		return batchEntity;
	}
}
