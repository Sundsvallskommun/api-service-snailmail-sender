package se.sundsvall.snailmail.integration.samba;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.zalando.problem.Problem;

import se.sundsvall.snailmail.Application;
import se.sundsvall.snailmail.api.model.EnvelopeType;
import se.sundsvall.snailmail.integration.db.model.AttachmentEntity;
import se.sundsvall.snailmail.integration.db.model.BatchEntity;
import se.sundsvall.snailmail.integration.db.model.DepartmentEntity;
import se.sundsvall.snailmail.integration.db.model.RecipientEntity;
import se.sundsvall.snailmail.integration.db.model.RequestEntity;

import jcifs.smb.SmbFile;

@ActiveProfiles("junit")
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@Testcontainers
class SambaIntegrationTest {

	@Container
	public static final DockerComposeContainer<?> sambaContainer = new DockerComposeContainer<>(new File("src/test/resources/docker/docker-compose.yml"))
		.withStartupTimeout(Duration.ofSeconds(60));

	private static final String SOME_DATA = "someData";

	private static final String DEPARTMENT_1 = "department1";

	private static final String BATCH_ID = "123e4567-e89b-12d3-a456-426614174000";

	private static final String CSV_DATA = """
		namn,careOf,adress,lagenhet,postnummer,postort
		Janne Långben,Some CareOf,Some Address 123,1101,123 45,ÖREBRO
		""";

	@Autowired
	private SambaIntegration sambaIntegration;

	private static @NotNull List<RequestEntity> getRequestEntities(final String name) {
		return List.of(
			RequestEntity.builder()
				.withRecipientEntity(
					RecipientEntity.builder()
						.withAddress("Some Address 123")
						.withCity("ÖREBRO")
						.withCareOf("Some CareOf")
						.withGivenName("Janne")
						.withLastName("Långben")
						.withPostalCode("123 45")
						.withApartmentNumber("1101")
						.build())
				.withAttachmentEntities(List.of(
					AttachmentEntity.builder()
						.withEnvelopeType(EnvelopeType.PLAIN)
						.withContent(Base64.getEncoder().encodeToString(SOME_DATA.getBytes()))
						.withName(name)
						.build()))
				.build());
	}

	private static BatchEntity getBatchEntity(final String batchId, final String name) {
		return BatchEntity.builder()
			.withId(batchId)
			.withDepartmentEntities(List.of(
				DepartmentEntity.builder()
					.withName(DEPARTMENT_1)
					.withRequestEntities(getRequestEntities(name))
					.build()))
			.build();
	}

	@Test
	void writeBatchDataToSambaShare() throws IOException {

		// Arrange
		final var smbPath = "smb://localhost:1445/share/" + DEPARTMENT_1 + "/" + BATCH_ID + "/sandlista-someName.csv";

		final var batchEntity = getBatchEntity(BATCH_ID, "someName.pdf");

		sambaIntegration.writeBatchDataToSambaShare(batchEntity);

		// Assert
		try (final var smbFile = new SmbFile(smbPath, sambaIntegration.getContext())) {
			assertThat(smbFile.exists()).isTrue();
		}

		// Read the samba file and verify content
		try (final var smbFile = new SmbFile(smbPath, sambaIntegration.getContext())) {
			final var content = new String(smbFile.getInputStream().readAllBytes(), StandardCharsets.ISO_8859_1);
			assertThat(content).isEqualTo(CSV_DATA);
		}
	}

	@Test
	void writeBatchDataToSambaShareWithIssuer() throws IOException {
		// Arrange
		final var issuer = "issuer";
		final var smbPath = "smb://localhost:1445/share/" + DEPARTMENT_1 + "/" + issuer + "_" + BATCH_ID + "/sandlista-someName.csv";

		final var batchEntity = getBatchEntity(BATCH_ID, "someName.pdf");
		batchEntity.setIssuer(issuer);

		sambaIntegration.writeBatchDataToSambaShare(batchEntity);

		// Assert
		try (final var smbFile = new SmbFile(smbPath, sambaIntegration.getContext())) {
			assertThat(smbFile.exists()).isTrue();
		}

		try (final var smbFile = new SmbFile(smbPath, sambaIntegration.getContext())) {
			final var content = new String(smbFile.getInputStream().readAllBytes(), StandardCharsets.ISO_8859_1);
			assertThat(content).isEqualTo(CSV_DATA);
		}
	}

	@Test
	void writeBatchDataToSambaShareFailedToCreateFolder() {

		final var batchEntity = getBatchEntity("asd/asd", "someName.pdf");

		assertThatThrownBy(() -> sambaIntegration.writeBatchDataToSambaShare(batchEntity))
			.isInstanceOf(Problem.class)
			.hasMessage("Internal Server Error: Failed to create folder smb://localhost:1445/share/department1/asd/asd on Samba share")
			.hasFieldOrPropertyWithValue("status", INTERNAL_SERVER_ERROR);
	}

	@Test
	void writeBatchDataToSambaShareFailedSaveAttachment() {

		final var batchEntity = getBatchEntity(BATCH_ID, "someName/pdf");

		assertThatThrownBy(() -> sambaIntegration.writeBatchDataToSambaShare(batchEntity))
			.isInstanceOf(Problem.class)
			.hasMessage("Internal Server Error: Failed to write attachmentEntity to Samba share")
			.hasFieldOrPropertyWithValue("status", INTERNAL_SERVER_ERROR);
	}

}
