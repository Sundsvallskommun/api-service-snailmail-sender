package se.sundsvall.snailmail.integration.emailsender;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import se.sundsvall.snailmail.dto.SnailMailDto;

@Component
@EnableConfigurationProperties(EmailSenderIntegrationProperties.class)
public class EmailSenderIntegration {

	static final String INTEGRATION_NAME = "Messaging";

	private final EmailSenderClient client;
	private final EmailSenderIntegrationMapper mapper;

	public EmailSenderIntegration(EmailSenderClient client, EmailSenderIntegrationMapper mapper) {
		this.client = client;
		this.mapper = mapper;
	}

	public void sendEmail(final SnailMailDto snailMailDto) {
		final var request = mapper.toSendEmailRequest(snailMailDto);
		client.sendEmail(request);
	}
}
