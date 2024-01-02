package se.sundsvall.snailmail;

import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import se.sundsvall.snailmail.api.model.EnvelopeType;
import se.sundsvall.snailmail.api.model.SendSnailMailRequest;
import se.sundsvall.snailmail.dto.SnailMailDto;
import se.sundsvall.snailmail.integration.emailsender.EmailSenderIntegrationProperties;

import generated.se.sundsvall.citizen.CitizenAddress;
import generated.se.sundsvall.citizen.CitizenExtended;

public final class TestDataFactory {

    public static SnailMailDto buildSnailMailDto() {

        return SnailMailDto.builder()
                .withDeviation("someDeviation")
                .withDepartment("someDepartment")
                .withBatchId("someBatchId")
                .withAttachments(List.of(SnailMailDto.AttachmentDto.builder()
                        .withContent("someContent")
                        .withName("someName")
                        .withContentType("someContentType")
                        .withEnvelopeType(EnvelopeType.PLAIN)
                        .build()))
                .build();
    }

    public static SnailMailDto buildSnailMailDtoWithBlankDeviation() {

        return SnailMailDto.builder()
                .withDeviation(" ")
                .withDepartment("someDepartment")
                .withAttachments(List.of(SnailMailDto.AttachmentDto.builder()
                        .withContent("someContent")
                        .withName("someName")
                        .withContentType("someContentType")
                        .withEnvelopeType(EnvelopeType.PLAIN)
                        .build()))
                .build();
    }

    public static SnailMailDto buildSnailMailDtoWithoutDeviation() {

        return SnailMailDto.builder()
                .withDepartment("someDepartment")
                .withAttachments(List.of(SnailMailDto.AttachmentDto.builder()
                        .withContent("someContent")
                        .withName("someName")
                        .withContentType("someContentType")
                        .build()))
                .build();
    }

    public static EmailSenderIntegrationProperties buildEmailProperties() {

        var properties = new EmailSenderIntegrationProperties();

        properties.setEmailAddress("some@email.se");

        var sender = new EmailSenderIntegrationProperties.Sender();
        sender.setAddress("someemail@host.se");
        sender.setName("someName");
        sender.setReplyTo("someoneToReplyTo");

        properties.setSender(sender);
        return properties;
    }

    public static SendSnailMailRequest buildSendSnailMailRequest() {
        return SendSnailMailRequest.builder()
                .withDepartment("someDepartment")
                .withDeviation("someDeviation")
                .withBatchId("someBatchId")
                .withPartyId("544ff3ae-e3c4-451f-bee5-d5ed480451f7")
                .withAttachments(List.of(buildAttachmentList()))
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
        var citizenExtended = new CitizenExtended();
        citizenExtended.setPersonId(UUID.randomUUID());
        citizenExtended.setGivenname("Kalle");
        citizenExtended.setLastname("Anka");
        citizenExtended.setGender("M");
        citizenExtended.setCivilStatus("OG");
        citizenExtended.setNrDate("20131125");
        citizenExtended.setClassified("N");
        citizenExtended.setProtectedNR("N");

        var citizenAddress = buildCitizenAddress();

        citizenExtended.setAddresses(List.of(citizenAddress));

        return citizenExtended;

    }

    private static CitizenAddress buildCitizenAddress() {
        var citizenAddress = new CitizenAddress();
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
