package se.sundsvall.snailmail.integration.emailsender;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.snailmail.TestDataFactory.buildSnailMailDto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import generated.se.sundsvall.emailsender.SendEmailRequest;
import se.sundsvall.snailmail.dto.SnailMailDto;

@ExtendWith(MockitoExtension.class)
class EmailSenderIntegrationTest {

	@Mock
	private EmailSenderClient mockClient;

	@Mock
	private EmailSenderIntegrationMapper mockMapper;

	@InjectMocks
	private EmailSenderIntegration integration;

	@Test
	void sendEmail() {
		final SnailMailDto dto = buildSnailMailDto();

		when(mockClient.sendEmail(any())).thenReturn(ResponseEntity.ok().build());
		when(mockMapper.toSendEmailRequest(dto)).thenReturn(new SendEmailRequest());

		integration.sendEmail(dto);

		verify(mockMapper, times(1)).toSendEmailRequest(any(SnailMailDto.class));
		verifyNoMoreInteractions(mockMapper);
		verify(mockClient, times(1)).sendEmail(any(SendEmailRequest.class));
		verifyNoMoreInteractions(mockClient);
	}
}
