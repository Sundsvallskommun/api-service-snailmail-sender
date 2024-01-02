package se.sundsvall.snailmail.service;

import static se.sundsvall.snailmail.service.Mapper.toSnailMailDto;

import org.springframework.stereotype.Service;

import se.sundsvall.dept44.util.jacoco.ExcludeFromJacocoGeneratedCoverageReport;
import se.sundsvall.snailmail.api.model.SendSnailMailRequest;
import se.sundsvall.snailmail.integration.emailsender.EmailSenderIntegration;

@Service
public class SnailMailService {

	private final EmailSenderIntegration emailSenderIntegration;

	public SnailMailService(final EmailSenderIntegration emailSenderIntegration) {
		this.emailSenderIntegration = emailSenderIntegration;
	}

	public void sendSnailMail(final SendSnailMailRequest request) {
		emailSenderIntegration.sendEmail(toSnailMailDto(request, null));
	}


	@ExcludeFromJacocoGeneratedCoverageReport
	public void sendBatch(final String batchId) {
		//Should create CSVs for batch and put them in the samba share
		// Implemented in different story
	}

}
