package se.sundsvall.snailmail.integration.samba;


import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;

import java.io.File;
import java.time.Duration;
import java.util.Base64;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
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

@ActiveProfiles("junit")
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@Testcontainers
class SambaIntegrationTest {

	@Container
	public static final DockerComposeContainer<?> sambaContainer =
		new DockerComposeContainer<>(new File("src/test/resources/docker/docker-compose.yml"))
			.withExposedService("samba", 445, Wait.forListeningPort())
			.withStartupTimeout(Duration.ofSeconds(60));

	@Autowired
	private SambaIntegration sambaIntegration;


	@Test
	void writeBatchDataToSambaShare() {

		final var batchEntity = BatchEntity.builder()
			.withId("123e4567-e89b-12d3-a456-426614174000")
			.withDepartmentEntities(List.of(
				DepartmentEntity.builder()
					.withName("department1")
					.withRequestEntities(List.of(
						RequestEntity.builder()
							.withRecipientEntity(
								RecipientEntity.builder()
									.build())
							.withAttachmentEntities(List.of(
								AttachmentEntity.builder()
									.withEnvelopeType(EnvelopeType.PLAIN)
									.withContent(Base64.getEncoder().encodeToString("someData".getBytes()))
									.withName("someName.pdf")
									.build()))
							.build()))
					.build()))
			.build();

		sambaIntegration.writeBatchDataToSambaShare(batchEntity);
	}

	@Test
	void writeBatchDataToSambaShare_failedToCreateFolder() {

		final var batchEntity = BatchEntity.builder()
			.withId("asd/asd")
			.withDepartmentEntities(List.of(
				DepartmentEntity.builder()
					.withName("department1")
					.withRequestEntities(List.of(
						RequestEntity.builder()
							.withRecipientEntity(
								RecipientEntity.builder()
									.build())
							.withAttachmentEntities(List.of(
								AttachmentEntity.builder()
									.withEnvelopeType(EnvelopeType.PLAIN)
									.withContent(Base64.getEncoder().encodeToString("someData".getBytes()))
									.withName("someName.pdf")
									.build()))
							.build()))
					.build()))
			.build();

		assertThatThrownBy(() -> sambaIntegration.writeBatchDataToSambaShare(batchEntity))
			.isInstanceOf(Problem.class)
			.hasMessage("Internal Server Error: Failed to create folder smb://localhost:445/share/department1/asd/asd on Samba share")
			.hasFieldOrPropertyWithValue("status", INTERNAL_SERVER_ERROR);
	}

	@Test
	void writeBatchDataToSambaShare_failedSaveAttachment() {

		final var batchEntity = BatchEntity.builder()
			.withId("123e4567-e89b-12d3-a456-426614174000")
			.withDepartmentEntities(List.of(
				DepartmentEntity.builder()
					.withName("department1")
					.withRequestEntities(List.of(
						RequestEntity.builder()
							.withRecipientEntity(
								RecipientEntity.builder()
									.build())
							.withAttachmentEntities(List.of(
								AttachmentEntity.builder()
									.withEnvelopeType(EnvelopeType.PLAIN)
									.withContent(Base64.getEncoder().encodeToString("someData".getBytes()))
									.withName("someName/pdf")
									.build()))
							.build()))
					.build()))
			.build();

		assertThatThrownBy(() -> sambaIntegration.writeBatchDataToSambaShare(batchEntity))
			.isInstanceOf(Problem.class)
			.hasMessage("Internal Server Error: Failed to write attachmentEntity to Samba share")
			.hasFieldOrPropertyWithValue("status", INTERNAL_SERVER_ERROR);
	}

}
