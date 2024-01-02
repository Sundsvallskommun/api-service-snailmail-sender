package se.sundsvall.snailmail.service;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.snailmail.TestDataFactory.buildCitizenExtended;
import static se.sundsvall.snailmail.TestDataFactory.buildSendSnailMailRequest;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.snailmail.dto.SnailMailDto;
import se.sundsvall.snailmail.integration.citizen.CitizenIntegration;
import se.sundsvall.snailmail.integration.samba.SambaIntegration;

@ExtendWith(MockitoExtension.class)
class SnailMailServiceTest {

	@Mock
	private SambaIntegration mockSambaIntegration;

	@Mock
	private CitizenIntegration mockCitizenIntegration;

	@InjectMocks
	private SnailMailService snailMailService;

	@Test
	void sendSnailMail() throws IOException {
		final var request = buildSendSnailMailRequest();

		when(mockCitizenIntegration.getCitizen(request.getPartyId())).thenReturn(buildCitizenExtended());
		doNothing().when(mockSambaIntegration).writeBatchDataToSambaShare(any(SnailMailDto.class));

		snailMailService.sendSnailMail(request);

		verify(mockCitizenIntegration).getCitizen(request.getPartyId());
		verify(mockSambaIntegration).writeBatchDataToSambaShare(any(SnailMailDto.class));
		verifyNoMoreInteractions(mockCitizenIntegration, mockSambaIntegration);
	}

	@Test
	void sendSnailMail_throwsExceptionWhenWritingToSamba() throws IOException {
		final var request = buildSendSnailMailRequest();

		when(mockCitizenIntegration.getCitizen(request.getPartyId())).thenReturn(buildCitizenExtended());
		doThrow(IOException.class).when(mockSambaIntegration).writeBatchDataToSambaShare(any(SnailMailDto.class));

		assertThatExceptionOfType(ThrowableProblem.class).isThrownBy(() -> snailMailService.sendSnailMail(request));

		verify(mockCitizenIntegration, times(1)).getCitizen(request.getPartyId());
		verify(mockSambaIntegration, times(1)).writeBatchDataToSambaShare(any(SnailMailDto.class));
		verifyNoMoreInteractions(mockCitizenIntegration, mockSambaIntegration);
	}

}
