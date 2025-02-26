package se.sundsvall.snailmail;

import java.util.Base64;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import se.sundsvall.snailmail.api.model.EnvelopeType;
import se.sundsvall.snailmail.api.model.SendSnailMailRequest;
import se.sundsvall.snailmail.integration.db.model.AttachmentEntity;
import se.sundsvall.snailmail.integration.db.model.BatchEntity;
import se.sundsvall.snailmail.integration.db.model.DepartmentEntity;
import se.sundsvall.snailmail.integration.db.model.RecipientEntity;
import se.sundsvall.snailmail.integration.db.model.RequestEntity;

public final class TestDataFactory {

	public static final String SOME_DATA = "someData";
	public static final String DEPARTMENT_1 = "department1";

	private TestDataFactory() {
		// Intentionally empty to prevent instantiation
	}

	public static SendSnailMailRequest buildSendSnailMailRequest() {
		return SendSnailMailRequest.builder()
			.withMunicipalityId("2281")
			.withIssuer("someIssuer")
			.withBatchId("550e8400-e29b-41d4-a716-446655440000")
			.withDepartment("someDepartment")
			.withDeviation("someDeviation")
			.withAttachments(List.of(buildAttachmentList()))
			.build();
	}

	public static SendSnailMailRequest.Address buildSendSnailMailAddress() {
		return SendSnailMailRequest.Address.builder()
			.withFirstName("Kalle")
			.withLastName("Anka")
			.withAddress("Ankeborgsvägen 1")
			.withZipCode("123 45")
			.withCity("ANKEBORG")
			.withCountry("SVERIGE")
			.withOrganizationNumber("1234567890")
			.withCareOf("John Doe")
			.build();
	}

	public static SendSnailMailRequest.Attachment buildAttachmentList() {
		return SendSnailMailRequest.Attachment.builder()
			.withContent("base64")
			.withContentType("application/pdf")
			.withName("filename.pdf")
			.withEnvelopeType(EnvelopeType.PLAIN)
			.build();
	}

	public static @NotNull List<RequestEntity> getRequestEntities(final String name) {
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

	public static BatchEntity getBatchEntity(final String batchId, final String name) {
		return BatchEntity.builder()
			.withId(batchId)
			.withMunicipalityId("2281")
			.withDepartmentEntities(List.of(
				DepartmentEntity.builder()
					.withName(DEPARTMENT_1)
					.withRequestEntities(getRequestEntities(name))
					.build()))
			.build();
	}

}
