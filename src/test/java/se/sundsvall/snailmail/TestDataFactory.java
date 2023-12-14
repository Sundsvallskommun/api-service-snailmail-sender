package se.sundsvall.snailmail;

import java.util.List;

import se.sundsvall.snailmail.api.model.EnvelopeType;
import se.sundsvall.snailmail.api.model.SendSnailMailRequest;
import se.sundsvall.snailmail.dto.SnailMailDto;
import se.sundsvall.snailmail.integration.emailsender.EmailSenderIntegrationProperties;

public final class TestDataFactory {

    public static SnailMailDto buildSnailMailDto() {

        return SnailMailDto.builder()
                .withDeviation("someDeviation")
                .withDepartment("someDepartment")
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
                .withAttachments(List.of(buildAttachmentList()))
                .build();

    }

    public static SendSnailMailRequest.Attachment buildAttachmentList() {
        return SendSnailMailRequest.Attachment.builder()
                .withContent("someContent")
                .withContentType("someContentType")
                .withName("someName")
                .withEnvelopeType(EnvelopeType.PLAIN)
                .build();
    }
}
