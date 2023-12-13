package se.sundsvall.snailmail.service;

import static se.sundsvall.snailmail.service.Mapper.toSnailMailDto;

import org.springframework.stereotype.Service;

import se.sundsvall.snailmail.api.model.SendSnailMailRequest;
import se.sundsvall.snailmail.integration.emailsender.EmailSenderIntegration;

@Service
public class SnailMailService {

	private final EmailSenderIntegration emailSenderIntegration;

	public SnailMailService(EmailSenderIntegration emailSenderIntegration) {
		this.emailSenderIntegration = emailSenderIntegration;
	}

	public void sendSnailMail(SendSnailMailRequest request) {
		emailSenderIntegration.sendEmail(toSnailMailDto(request, null));
	}
}
