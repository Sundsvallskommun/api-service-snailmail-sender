package se.sundsvall.snailmail;

import java.util.List;
import se.sundsvall.snailmail.api.model.EnvelopeType;
import se.sundsvall.snailmail.api.model.SendSnailMailRequest;

public final class TestDataFactory {

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
}
