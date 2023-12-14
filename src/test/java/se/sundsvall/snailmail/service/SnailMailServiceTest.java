package se.sundsvall.snailmail.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static se.sundsvall.snailmail.TestDataFactory.buildSendSnailMailRequest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.snailmail.dto.SnailMailDto;
import se.sundsvall.snailmail.integration.emailsender.EmailSenderIntegration;

@ExtendWith(MockitoExtension.class)
class SnailMailServiceTest {

	@Mock
	private EmailSenderIntegration mockEmailSenderIntegration;

	@InjectMocks
	private SnailMailService snailMailService;

	@Test
	void sendMail() {
		doNothing().when(mockEmailSenderIntegration).sendEmail(any());
		snailMailService.sendSnailMail(buildSendSnailMailRequest());
		verify(mockEmailSenderIntegration, times(1)).sendEmail(any(SnailMailDto.class));
		verifyNoMoreInteractions(mockEmailSenderIntegration);
	}
}
