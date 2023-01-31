package se.sundsvall.snailmail.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.snailmail.api.model.SendSnailMailRequest;
import se.sundsvall.snailmail.dto.SnailMailDto;
import se.sundsvall.snailmail.integration.emailsender.EmailSenderIntegration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static se.sundsvall.snailmail.TestDataFactory.buildSendSnailMailRequest;

@ExtendWith(MockitoExtension.class)
class SnailMailServiceTest {

    @Mock
    EmailSenderIntegration mockEmailSenderIntegration;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    Mapper mockMapper;

    SnailMailService snailMailService;


    @BeforeEach
    void setup() {
        snailMailService = new SnailMailService(mockEmailSenderIntegration, mockMapper);

    }

    @Test
    void sendMail() {
        doNothing().when(mockEmailSenderIntegration).sendEmail(any());
        snailMailService.sendSnailMail(buildSendSnailMailRequest());
        verify(mockEmailSenderIntegration, times(1)).sendEmail(any(SnailMailDto.class));
        verifyNoMoreInteractions(mockEmailSenderIntegration);
        verify(mockMapper, times(1)).toSnailMailDto(any(SendSnailMailRequest.class), any());
        verify(mockMapper, times(1)).toAttachmentDto(any(SendSnailMailRequest.Attachment.class));
        verifyNoMoreInteractions(mockMapper);
    }
}