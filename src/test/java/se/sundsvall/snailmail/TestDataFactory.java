package se.sundsvall.snailmail;

import generated.se.sundsvall.citizen.CitizenAddress;
import generated.se.sundsvall.citizen.CitizenExtended;
import java.util.List;
import java.util.UUID;
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
			.withPartyId("6ba7b810-9dad-11d1-80b4-00c04fd430c8")
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

	public static CitizenExtended buildCitizenExtended() {
		final var citizenExtended = new CitizenExtended();
		citizenExtended.setPersonId(UUID.randomUUID());
		citizenExtended.setGivenname("Kalle");
		citizenExtended.setLastname("Anka");
		citizenExtended.setGender("M");
		citizenExtended.setCivilStatus("OG");
		citizenExtended.setNrDate("20131125");
		citizenExtended.setClassified("N");
		citizenExtended.setProtectedNR("N");

		final var citizenAddress = buildCitizenAddress();

		citizenExtended.setAddresses(List.of(citizenAddress));

		return citizenExtended;

	}

	private static CitizenAddress buildCitizenAddress() {
		final var citizenAddress = new CitizenAddress();
		citizenAddress.setRealEstateDescription("Ankeborg 1:80");
		citizenAddress.setAddress("Ankeborgsvägen 1");
		citizenAddress.setAppartmentNumber("LGH 123");
		citizenAddress.setPostalCode("123 45");
		citizenAddress.setCity("ANKEBORG");
		citizenAddress.setMunicipality("2281");
		citizenAddress.setCountry("SVERIGE");
		citizenAddress.setEmigrated(false);
		citizenAddress.setAddressType("POPULATION_REGISTRATION_ADDRESS");
		citizenAddress.setCo("Kajsa Anka");
		return citizenAddress;
	}
}
