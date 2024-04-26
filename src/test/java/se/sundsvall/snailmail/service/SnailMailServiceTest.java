package se.sundsvall.snailmail.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import se.sundsvall.snailmail.integration.db.model.BatchEntity;
import se.sundsvall.snailmail.integration.db.model.DepartmentEntity;
import se.sundsvall.snailmail.integration.db.model.RequestEntity;
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
		//Arrange
		var batchEntity = BatchEntity.builder().build();
		when(batchRepositoryMock.findById(any(String.class))).thenReturn(Optional.ofNullable(batchEntity));
		when(departmentRepositoryMock.findByNameAndBatchEntity(any(String.class), any(BatchEntity.class))).thenReturn(Optional.ofNullable(DepartmentEntity.builder().build()));
		when(citizenIntegrationMock.getCitizen(any(String.class))).thenReturn(buildCitizenExtended());

		//Act
		snailMailService.sendSnailMail(buildSendSnailMailRequest());

		//Assert
		verify(batchRepositoryMock).findById(any(String.class));
		verify(departmentRepositoryMock).findByNameAndBatchEntity("someDepartment", batchEntity);
		verify(requestRepositoryMock).save(any());
		verify(citizenIntegrationMock).getCitizen(any(String.class));
		verifyNoMoreInteractions(batchRepositoryMock, departmentRepositoryMock, requestRepositoryMock, citizenIntegrationMock);
		verifyNoInteractions(sambaIntegrationMock);

	}

	@Test
	void sendMailWithNewBatch() {
		//Arrange
		var batchEntity = BatchEntity.builder().build();
		var departmentEntity = DepartmentEntity.builder().build();
		when(citizenIntegrationMock.getCitizen(any(String.class))).thenReturn(buildCitizenExtended());
		when(batchRepositoryMock.findById(anyString())).thenReturn(Optional.empty());
		when(batchRepositoryMock.save(any(BatchEntity.class))).thenReturn(batchEntity);

		when(departmentRepositoryMock.findByNameAndBatchEntity(anyString(), any(BatchEntity.class))).thenReturn(Optional.empty());
		when(departmentRepositoryMock.save(any(DepartmentEntity.class))).thenReturn(departmentEntity);

		when(requestRepositoryMock.save(any(RequestEntity.class))).thenReturn(RequestEntity.builder().build());

		//Act
		snailMailService.sendSnailMail(buildSendSnailMailRequest());

		//Assert
		verify(citizenIntegrationMock).getCitizen(any(String.class));
		verify(batchRepositoryMock).findById(any(String.class));
		verify(batchRepositoryMock).save(any(BatchEntity.class));

		verify(departmentRepositoryMock).findByNameAndBatchEntity("someDepartment", batchEntity);
		verify(departmentRepositoryMock).save(any(DepartmentEntity.class));

		verify(requestRepositoryMock).save(any(RequestEntity.class));
		verifyNoMoreInteractions(batchRepositoryMock, departmentRepositoryMock, requestRepositoryMock, citizenIntegrationMock);
		verifyNoInteractions(sambaIntegrationMock);

	}

	@Test
	void sendMailWithNewDepartment() {
		//Arrange
		when(citizenIntegrationMock.getCitizen(any(String.class))).thenReturn(buildCitizenExtended());

		var batchEntity = BatchEntity.builder().build();
		when(batchRepositoryMock.findById(any(String.class))).thenReturn(Optional.ofNullable(batchEntity));
		when(departmentRepositoryMock.findByNameAndBatchEntity(any(String.class), any(BatchEntity.class))).thenReturn(Optional.empty());
		when(departmentRepositoryMock.save(any(DepartmentEntity.class))).thenReturn(DepartmentEntity.builder().build());
		when(requestRepositoryMock.save(any(RequestEntity.class))).thenReturn(RequestEntity.builder().build());

		//Act
		snailMailService.sendSnailMail(buildSendSnailMailRequest());

		//Assert
		verify(citizenIntegrationMock).getCitizen(any(String.class));
		verify(batchRepositoryMock).findById(any(String.class));
		verify(departmentRepositoryMock).findByNameAndBatchEntity(anyString(), any(BatchEntity.class));
		verify(departmentRepositoryMock).save(any(DepartmentEntity.class));
		verify(requestRepositoryMock).save(any(RequestEntity.class));
		verifyNoMoreInteractions(batchRepositoryMock, departmentRepositoryMock, requestRepositoryMock, citizenIntegrationMock);
		verifyNoInteractions(sambaIntegrationMock);

	}

	@Test
	void sendMailWithNewBatchAndDepartment() {
		when(citizenIntegrationMock.getCitizen(any(String.class))).thenReturn(buildCitizenExtended());
		when(batchRepositoryMock.findById(any(String.class))).thenReturn(Optional.empty());
		when(batchRepositoryMock.save(any(BatchEntity.class))).thenReturn(BatchEntity.builder().build());
		when(departmentRepositoryMock.findByNameAndBatchEntity(any(String.class), any(BatchEntity.class))).thenReturn(Optional.empty());
		when(departmentRepositoryMock.save(any(DepartmentEntity.class))).thenReturn(DepartmentEntity.builder().build());

		snailMailService.sendSnailMail(buildSendSnailMailRequest());

		verify(batchRepositoryMock).findById(any(String.class));
		verify(batchRepositoryMock).save(any(BatchEntity.class));
		verify(departmentRepositoryMock).findByNameAndBatchEntity(anyString(), any(BatchEntity.class));
		verify(departmentRepositoryMock).save(any(DepartmentEntity.class));
		verify(requestRepositoryMock).save(any(RequestEntity.class));
		verify(citizenIntegrationMock).getCitizen(any(String.class));
		verifyNoMoreInteractions(batchRepositoryMock, departmentRepositoryMock, requestRepositoryMock, citizenIntegrationMock);
		verifyNoInteractions(sambaIntegrationMock);

	}

	@Test
	void sendMailWithNewBatchAndDepartmentAndCitizen() {
		when(citizenIntegrationMock.getCitizen(any(String.class))).thenReturn(null);
		when(batchRepositoryMock.findById(any(String.class))).thenReturn(Optional.empty());
		when(batchRepositoryMock.save(any(BatchEntity.class))).thenReturn(BatchEntity.builder().build());
		when(departmentRepositoryMock.findByNameAndBatchEntity(any(String.class), any(BatchEntity.class))).thenReturn(Optional.empty());
		when(departmentRepositoryMock.save(any(DepartmentEntity.class))).thenReturn(DepartmentEntity.builder().build());

		snailMailService.sendSnailMail(buildSendSnailMailRequest());

		verify(batchRepositoryMock).findById(any(String.class));
		verify(batchRepositoryMock).save(any(BatchEntity.class));
		verify(departmentRepositoryMock).findByNameAndBatchEntity(any(String.class), any(BatchEntity.class));
		verify(departmentRepositoryMock).save(any(DepartmentEntity.class));
		verify(requestRepositoryMock).save(any(RequestEntity.class));
		verify(citizenIntegrationMock).getCitizen(any(String.class));
		verifyNoMoreInteractions(batchRepositoryMock, departmentRepositoryMock, requestRepositoryMock, citizenIntegrationMock);
		verifyNoInteractions(sambaIntegrationMock);

	}

	@Test
	void sendBatch() {
		var batchEntity = BatchEntity.builder().build();
		var batchId = "batchId";

		when(batchRepositoryMock.findById(batchId)).thenReturn(Optional.ofNullable(batchEntity));

		snailMailService.sendBatch(batchId);

		verify(batchRepositoryMock).findById(batchId);
		verify(sambaIntegrationMock).writeBatchDataToSambaShare(any(BatchEntity.class));
		verify(batchRepositoryMock).delete(any(BatchEntity.class));
		verifyNoMoreInteractions(batchRepositoryMock, sambaIntegrationMock);
		verifyNoInteractions(departmentRepositoryMock, requestRepositoryMock, citizenIntegrationMock);
	}

	@Test
	void sendBatchWithNoBatchFound() {

		var batchId = "batchId";

		when(batchRepositoryMock.findById(batchId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> snailMailService.sendBatch(batchId))
			.isInstanceOf(Problem.class)
			.hasMessage("No batch found: Failed to fetch batch data from database");

		verify(batchRepositoryMock).findById(batchId);
		verifyNoMoreInteractions(batchRepositoryMock);
		verifyNoInteractions(departmentRepositoryMock, sambaIntegrationMock, requestRepositoryMock, citizenIntegrationMock);
	}

	@Test
	void sendBatchWithoutEvelopeType() {
		// Arrange
		var batchEntity = BatchEntity.builder().build();
		var request = buildSendSnailMailRequest();
		request.getAttachments().getFirst().setEnvelopeType(null);

		// Mock
		when(batchRepositoryMock.findById(any(String.class))).thenReturn(Optional.ofNullable(batchEntity));
		when(departmentRepositoryMock.findByNameAndBatchEntity(any(String.class), any(BatchEntity.class))).thenReturn(Optional.ofNullable(DepartmentEntity.builder().build()));
		when(citizenIntegrationMock.getCitizen(any(String.class))).thenReturn(buildCitizenExtended());

		// Act
		snailMailService.sendSnailMail(request);

		// Verify
		verify(batchRepositoryMock).findById(any(String.class));
		verify(departmentRepositoryMock).findByNameAndBatchEntity(any(String.class), any(BatchEntity.class));
		verify(requestRepositoryMock).save(any());
		verify(citizenIntegrationMock).getCitizen(any(String.class));
		verifyNoMoreInteractions(batchRepositoryMock, departmentRepositoryMock, requestRepositoryMock, citizenIntegrationMock);
		verifyNoInteractions(sambaIntegrationMock);

	}

}
