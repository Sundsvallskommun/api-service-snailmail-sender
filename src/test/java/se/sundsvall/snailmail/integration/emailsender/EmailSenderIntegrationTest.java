package se.sundsvall.snailmail.integration.emailsender;

import generated.se.sundsvall.emailsender.SendEmailRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import se.sundsvall.snailmail.dto.SnailMailDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.snailmail.TestDataFactory.buildSnailMailDto;

@ExtendWith(MockitoExtension.class)
class EmailSenderIntegrationTest {


    EmailSenderIntegration integration;
    @Mock
    private EmailSenderClient mockClient;
    @Mock
    private EmailSenderIntegrationMapper mockMapper;

    @BeforeEach
    void setUp() {

        integration = new EmailSenderIntegration(mockClient, mockMapper);
    }

    @Test
    void sendEmail() {
        SnailMailDto dto = buildSnailMailDto();

        when(mockClient.sendEmail(any())).thenReturn(ResponseEntity.ok().build());
        when(mockMapper.toSendEmailRequest(dto)).thenReturn(new SendEmailRequest());

        integration.sendEmail(dto);

        verify(mockMapper, times(1)).toSendEmailRequest(any(SnailMailDto.class));
        verifyNoMoreInteractions(mockMapper);
        verify(mockClient, times(1)).sendEmail(any(SendEmailRequest.class));
        verifyNoMoreInteractions(mockClient);
    }
}