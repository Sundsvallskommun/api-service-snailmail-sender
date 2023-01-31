package se.sundsvall.snailmail.integration.emailsender;

import generated.se.sundsvall.emailsender.Attachment;
import generated.se.sundsvall.emailsender.SendEmailRequest;
import generated.se.sundsvall.emailsender.Sender;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import se.sundsvall.snailmail.dto.SnailMailDto;

import java.util.Base64;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@EnableConfigurationProperties(EmailSenderIntegrationProperties.class)
public class EmailSenderIntegrationMapper {

    private final EmailSenderIntegrationProperties properties;

    public EmailSenderIntegrationMapper(EmailSenderIntegrationProperties properties) {
        this.properties = properties;
    }

    public SendEmailRequest toSendEmailRequest(final SnailMailDto snailMailDto) {
        if (snailMailDto == null) {
            return null;
        }

        var attachments = Optional.ofNullable(snailMailDto.getAttachments()).stream()
                .flatMap(Collection::stream)
                .map(attachment -> new Attachment()
                        .content(attachment.getContent())
                        .contentType(attachment.getContentType())
                        .name(attachment.getName()))
                .toList();

        return new SendEmailRequest()
                .subject(buildSubjectLine(snailMailDto))
                .emailAddress(properties.getEmailAddress())
                //Used to set body to TEST if in test-environment
                .htmlMessage(Base64.getEncoder().encodeToString(Optional.ofNullable(properties.getEnvironment()).orElse("").getBytes()))
                .sender(new Sender()
                        .name(properties.getSender().getName())
                        .address(properties.getSender().getAddress())
                        .replyTo(properties.getSender().getReplyTo()))
                .attachments(attachments);
    }

    private String buildSubjectLine(SnailMailDto dto) {
        return Stream.of("Utgående post", dto.getDepartment(), dto.getDeviation(), properties.getSender().getReplyTo())
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(" - "));
    }
}

