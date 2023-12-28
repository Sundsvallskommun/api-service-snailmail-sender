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
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.snailmail.api.model.SendSnailMailRequest;
import se.sundsvall.snailmail.integration.citizen.CitizenIntegration;
import se.sundsvall.snailmail.integration.db.BatchRepository;
import se.sundsvall.snailmail.integration.db.DepartmentRepository;
import se.sundsvall.snailmail.integration.db.RequestRepository;
import se.sundsvall.snailmail.integration.db.model.Batch;
import se.sundsvall.snailmail.integration.db.model.Department;
import se.sundsvall.snailmail.integration.db.model.Request;
import se.sundsvall.snailmail.integration.samba.SambaIntegration;

@ExtendWith(MockitoExtension.class)
class SnailMailServiceTest {

	@Mock
	private CitizenIntegration citizenIntegrationMock;

	@Mock
	private BatchRepository batchRepositoryMock;

	@Mock
	private DepartmentRepository departmentRepositoryMock;

	@Mock
	private RequestRepository requestRepositoryMock;

	@Mock
	private SambaIntegration mockSambaIntegration;

	@InjectMocks
	private SnailMailService snailMailService;

	@Test
	void sendMail() {

		when(batchRepositoryMock.findById(any(String.class))).thenReturn(Optional.ofNullable(Batch.builder().build()));
		when(departmentRepositoryMock.findByName(any(String.class))).thenReturn(Optional.ofNullable(Department.builder().build()));
		when(citizenIntegrationMock.getCitizen(any(String.class))).thenReturn(buildCitizenExtended());

		snailMailService.sendSnailMail(buildSendSnailMailRequest());

		verify(batchRepositoryMock, times(1)).findById(any(String.class));
		verify(departmentRepositoryMock, times(1)).findByName(any(String.class));
		verify(requestRepositoryMock, times(1)).save(any());
		verify(citizenIntegrationMock, times(1)).getCitizen(any(String.class));
		verifyNoMoreInteractions(batchRepositoryMock, departmentRepositoryMock, requestRepositoryMock, citizenIntegrationMock);

	}

	@Test
	void sendMailWithNewBatch() {

		when(batchRepositoryMock.findById(any(String.class))).thenReturn(Optional.empty());
		when(departmentRepositoryMock.findByName(any(String.class))).thenReturn(Optional.ofNullable(Department.builder().build()));
		when(citizenIntegrationMock.getCitizen(any(String.class))).thenReturn(buildCitizenExtended());

		snailMailService.sendSnailMail(buildSendSnailMailRequest());

		verify(batchRepositoryMock, times(1)).findById(any(String.class));
		verify(batchRepositoryMock, times(1)).save(any(Batch.class));
		verify(departmentRepositoryMock, times(1)).findByName(any(String.class));
		verify(requestRepositoryMock, times(1)).save(any(Request.class));
		verify(citizenIntegrationMock, times(1)).getCitizen(any(String.class));
		verifyNoMoreInteractions(batchRepositoryMock, departmentRepositoryMock, requestRepositoryMock, citizenIntegrationMock);

	}

	@Test
	void sendMailWithNewDepartment() {

		when(batchRepositoryMock.findById(any(String.class))).thenReturn(Optional.ofNullable(Batch.builder().build()));
		when(departmentRepositoryMock.findByName(any(String.class))).thenReturn(Optional.empty());
		when(citizenIntegrationMock.getCitizen(any(String.class))).thenReturn(buildCitizenExtended());
		snailMailService.sendSnailMail(buildSendSnailMailRequest());

		verify(batchRepositoryMock, times(1)).findById(any(String.class));
		verify(departmentRepositoryMock, times(1)).findByName(any(String.class));
		verify(departmentRepositoryMock, times(1)).save(any(Department.class));
		verify(requestRepositoryMock, times(1)).save(any(Request.class));
		verify(citizenIntegrationMock, times(1)).getCitizen(any(String.class));
		verifyNoMoreInteractions(batchRepositoryMock, departmentRepositoryMock, requestRepositoryMock, citizenIntegrationMock);

	}

	@Test
	void sendMailWithNewBatchAndDepartment() {

		when(batchRepositoryMock.findById(any(String.class))).thenReturn(Optional.empty());
		when(departmentRepositoryMock.findByName(any(String.class))).thenReturn(Optional.empty());
		when(citizenIntegrationMock.getCitizen(any(String.class))).thenReturn(buildCitizenExtended());

		snailMailService.sendSnailMail(buildSendSnailMailRequest());

		verify(batchRepositoryMock, times(1)).findById(any(String.class));
		verify(batchRepositoryMock, times(1)).save(any(Batch.class));
		verify(departmentRepositoryMock, times(1)).findByName(any(String.class));
		verify(departmentRepositoryMock, times(1)).save(any(Department.class));
		verify(requestRepositoryMock, times(1)).save(any(Request.class));
		verify(citizenIntegrationMock, times(1)).getCitizen(any(String.class));
		verifyNoMoreInteractions(batchRepositoryMock, departmentRepositoryMock, requestRepositoryMock, citizenIntegrationMock);

	}

	@Test
	void sendMailWithNewBatchAndDepartmentAndCitizen() {

		when(batchRepositoryMock.findById(any(String.class))).thenReturn(Optional.empty());
		when(departmentRepositoryMock.findByName(any(String.class))).thenReturn(Optional.empty());
		when(citizenIntegrationMock.getCitizen(any(String.class))).thenReturn(null);

		snailMailService.sendSnailMail(buildSendSnailMailRequest());

		verify(batchRepositoryMock, times(1)).findById(any(String.class));
		verify(batchRepositoryMock, times(1)).save(any(Batch.class));
		verify(departmentRepositoryMock, times(1)).findByName(any(String.class));
		verify(departmentRepositoryMock, times(1)).save(any(Department.class));
		verify(requestRepositoryMock, times(1)).save(any(Request.class));
		verify(citizenIntegrationMock, times(1)).getCitizen(any(String.class));
		verifyNoMoreInteractions(batchRepositoryMock, departmentRepositoryMock, requestRepositoryMock, citizenIntegrationMock);

	}

	@Test
	void testSaveSnailMailForBatch() throws IOException {
		final var request = buildSendSnailMailRequest();

		doNothing().when(mockSambaIntegration).writeBatchDataToSambaShare(any(SendSnailMailRequest.class));

		snailMailService.saveSnailMailForBatch(request);

		verify(mockSambaIntegration).writeBatchDataToSambaShare(any(SendSnailMailRequest.class));
		verifyNoMoreInteractions(citizenIntegrationMock, mockSambaIntegration);
	}

	@Test
	void testSaveSnailMailForBatch_throwsExceptionWhenWritingToSamba() throws IOException {
		final var request = buildSendSnailMailRequest();

		doThrow(IOException.class).when(mockSambaIntegration).writeBatchDataToSambaShare(any(SendSnailMailRequest.class));

		assertThatExceptionOfType(ThrowableProblem.class).isThrownBy(() -> snailMailService.saveSnailMailForBatch(request));

		verify(mockSambaIntegration, times(1)).writeBatchDataToSambaShare(any(SendSnailMailRequest.class));
		verifyNoMoreInteractions(citizenIntegrationMock, mockSambaIntegration);
	}

}
