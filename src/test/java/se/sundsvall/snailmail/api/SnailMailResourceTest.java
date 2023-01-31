package se.sundsvall.snailmail.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import se.sundsvall.snailmail.api.model.SendSnailMailRequest;
import se.sundsvall.snailmail.service.SnailMailService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static se.sundsvall.snailmail.TestDataFactory.buildSendSnailMailRequest;


@ExtendWith(MockitoExtension.class)
class SnailMailResourceTest {

    @Mock
    SnailMailService mockSnailMailService;

    SnailMailResource snailMailResource;

    @BeforeEach
    void setUp() {
        snailMailResource = new SnailMailResource(mockSnailMailService);
    }

    @Test
    void sendSnailMail() {
        doNothing().when(mockSnailMailService).sendSnailMail(any(SendSnailMailRequest.class));

        var response = snailMailResource.sendSnailMail( buildSendSnailMailRequest());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        verify(mockSnailMailService, times(1)).sendSnailMail(any(SendSnailMailRequest.class));

    }
}