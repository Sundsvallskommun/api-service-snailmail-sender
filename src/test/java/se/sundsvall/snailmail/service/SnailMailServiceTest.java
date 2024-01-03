package se.sundsvall.snailmail.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.snailmail.TestDataFactory.buildCitizenExtended;
import static se.sundsvall.snailmail.TestDataFactory.buildSendSnailMailRequest;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Problem;

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
	private SambaIntegration sambaIntegrationMock;

	@InjectMocks
	private SnailMailService snailMailService;

	@Test
	void sendMail() {

		when(batchRepositoryMock.findById(any(String.class))).thenReturn(Optional.ofNullable(Batch.builder().build()));
		when(departmentRepositoryMock.findByName(any(String.class))).thenReturn(Optional.ofNullable(Department.builder().build()));
		when(citizenIntegrationMock.getCitizen(any(String.class))).thenReturn(buildCitizenExtended());

		snailMailService.sendSnailMail(buildSendSnailMailRequest());

		verify(batchRepositoryMock).findById(any(String.class));
		verify(departmentRepositoryMock).findByName(any(String.class));
		verify(requestRepositoryMock).save(any());
		verify(citizenIntegrationMock).getCitizen(any(String.class));
		verifyNoMoreInteractions(batchRepositoryMock, departmentRepositoryMock, requestRepositoryMock, citizenIntegrationMock);
		verifyNoInteractions(sambaIntegrationMock);

	}

	@Test
	void sendMailWithNewBatch() {

		when(batchRepositoryMock.findById(any(String.class))).thenReturn(Optional.empty());
		when(departmentRepositoryMock.findByName(any(String.class))).thenReturn(Optional.ofNullable(Department.builder().build()));
		when(citizenIntegrationMock.getCitizen(any(String.class))).thenReturn(buildCitizenExtended());

		snailMailService.sendSnailMail(buildSendSnailMailRequest());

		verify(batchRepositoryMock).findById(any(String.class));
		verify(batchRepositoryMock).save(any(Batch.class));
		verify(departmentRepositoryMock).findByName(any(String.class));
		verify(requestRepositoryMock).save(any(Request.class));
		verify(citizenIntegrationMock).getCitizen(any(String.class));
		verifyNoMoreInteractions(batchRepositoryMock, departmentRepositoryMock, requestRepositoryMock, citizenIntegrationMock);
		verifyNoInteractions(sambaIntegrationMock);

	}

	@Test
	void sendMailWithNewDepartment() {

		when(batchRepositoryMock.findById(any(String.class))).thenReturn(Optional.ofNullable(Batch.builder().build()));
		when(departmentRepositoryMock.findByName(any(String.class))).thenReturn(Optional.empty());
		when(citizenIntegrationMock.getCitizen(any(String.class))).thenReturn(buildCitizenExtended());
		snailMailService.sendSnailMail(buildSendSnailMailRequest());

		verify(batchRepositoryMock).findById(any(String.class));
		verify(departmentRepositoryMock).findByName(any(String.class));
		verify(departmentRepositoryMock).save(any(Department.class));
		verify(requestRepositoryMock).save(any(Request.class));
		verify(citizenIntegrationMock).getCitizen(any(String.class));
		verifyNoMoreInteractions(batchRepositoryMock, departmentRepositoryMock, requestRepositoryMock, citizenIntegrationMock);
		verifyNoInteractions(sambaIntegrationMock);

	}

	@Test
	void sendMailWithNewBatchAndDepartment() {

		when(batchRepositoryMock.findById(any(String.class))).thenReturn(Optional.empty());
		when(departmentRepositoryMock.findByName(any(String.class))).thenReturn(Optional.empty());
		when(citizenIntegrationMock.getCitizen(any(String.class))).thenReturn(buildCitizenExtended());

		snailMailService.sendSnailMail(buildSendSnailMailRequest());

		verify(batchRepositoryMock).findById(any(String.class));
		verify(batchRepositoryMock).save(any(Batch.class));
		verify(departmentRepositoryMock).findByName(any(String.class));
		verify(departmentRepositoryMock).save(any(Department.class));
		verify(requestRepositoryMock).save(any(Request.class));
		verify(citizenIntegrationMock).getCitizen(any(String.class));
		verifyNoMoreInteractions(batchRepositoryMock, departmentRepositoryMock, requestRepositoryMock, citizenIntegrationMock);
		verifyNoInteractions(sambaIntegrationMock);

	}

	@Test
	void sendMailWithNewBatchAndDepartmentAndCitizen() {

		when(batchRepositoryMock.findById(any(String.class))).thenReturn(Optional.empty());
		when(departmentRepositoryMock.findByName(any(String.class))).thenReturn(Optional.empty());
		when(citizenIntegrationMock.getCitizen(any(String.class))).thenReturn(null);

		snailMailService.sendSnailMail(buildSendSnailMailRequest());

		verify(batchRepositoryMock).findById(any(String.class));
		verify(batchRepositoryMock).save(any(Batch.class));
		verify(departmentRepositoryMock).findByName(any(String.class));
		verify(departmentRepositoryMock).save(any(Department.class));
		verify(requestRepositoryMock).save(any(Request.class));
		verify(citizenIntegrationMock).getCitizen(any(String.class));
		verifyNoMoreInteractions(batchRepositoryMock, departmentRepositoryMock, requestRepositoryMock, citizenIntegrationMock);
		verifyNoInteractions(sambaIntegrationMock);

	}

	@Test
	void sendBatch() {

		final var batchId = "batchId";

		when(batchRepositoryMock.findById(batchId)).thenReturn(Optional.ofNullable(Batch.builder().build()));

		snailMailService.sendBatch(batchId);

		verify(batchRepositoryMock).findById(batchId);
		verify(sambaIntegrationMock).writeBatchDataToSambaShare(any(Batch.class));
		verify(batchRepositoryMock).delete(any(Batch.class));
		verifyNoMoreInteractions(batchRepositoryMock, sambaIntegrationMock);
		verifyNoInteractions(departmentRepositoryMock, requestRepositoryMock, citizenIntegrationMock);
	}

	@Test
	void sendBatchWithNoBatchFound() {

		final var batchId = "batchId";

		when(batchRepositoryMock.findById(batchId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> snailMailService.sendBatch(batchId))
			.isInstanceOf(Problem.class)
			.hasMessage("No batch found: Failed to fetch batch data from database");

		verify(batchRepositoryMock).findById(batchId);
		verifyNoMoreInteractions(batchRepositoryMock);
		verifyNoInteractions(departmentRepositoryMock, sambaIntegrationMock, requestRepositoryMock, citizenIntegrationMock);
	}

}
