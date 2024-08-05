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
		final var batchEntity = BatchEntity.builder().build();
		final var municipalityId = "municipalityId";
		final var request = buildSendSnailMailRequest();
		when(batchRepositoryMock.findByMunicipalityIdAndId(municipalityId, request.getBatchId())).thenReturn(Optional.ofNullable(batchEntity));
		when(departmentRepositoryMock.findByNameAndBatchEntity(any(String.class), any(BatchEntity.class))).thenReturn(Optional.ofNullable(DepartmentEntity.builder().build()));
		when(citizenIntegrationMock.getCitizen(any(String.class))).thenReturn(buildCitizenExtended());

		//Act
		snailMailService.sendSnailMail(municipalityId, request);

		//Assert
		verify(batchRepositoryMock).findByMunicipalityIdAndId(municipalityId, request.getBatchId());
		verify(departmentRepositoryMock).findByNameAndBatchEntity("someDepartment", batchEntity);
		verify(requestRepositoryMock).save(any(RequestEntity.class));
		verify(citizenIntegrationMock).getCitizen(any(String.class));
		verifyNoMoreInteractions(batchRepositoryMock, departmentRepositoryMock, requestRepositoryMock, citizenIntegrationMock);
		verifyNoInteractions(sambaIntegrationMock);

	}

	@Test
	void sendMailWithNewBatch() {
		//Arrange
		final var batchEntity = BatchEntity.builder().build();
		final var departmentEntity = DepartmentEntity.builder().build();
		final var municipalityId = "municipalityId";
		final var request = buildSendSnailMailRequest();

		when(citizenIntegrationMock.getCitizen(any(String.class))).thenReturn(buildCitizenExtended());
		when(batchRepositoryMock.findByMunicipalityIdAndId(municipalityId, request.getBatchId())).thenReturn(Optional.empty());
		when(batchRepositoryMock.save(any(BatchEntity.class))).thenReturn(batchEntity);

		when(departmentRepositoryMock.findByNameAndBatchEntity(anyString(), any(BatchEntity.class))).thenReturn(Optional.empty());
		when(departmentRepositoryMock.save(any(DepartmentEntity.class))).thenReturn(departmentEntity);

		when(requestRepositoryMock.save(any(RequestEntity.class))).thenReturn(RequestEntity.builder().build());

		//Act
		snailMailService.sendSnailMail(municipalityId, request);

		//Assert
		verify(citizenIntegrationMock).getCitizen(any(String.class));
		verify(batchRepositoryMock).findByMunicipalityIdAndId(municipalityId, request.getBatchId());
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
		final var municipalityId = "municipalityId";
		final var batchEntity = BatchEntity.builder().build();
		final var request = buildSendSnailMailRequest();

		when(citizenIntegrationMock.getCitizen(any(String.class))).thenReturn(buildCitizenExtended());
		when(batchRepositoryMock.findByMunicipalityIdAndId(municipalityId, request.getBatchId())).thenReturn(Optional.ofNullable(batchEntity));
		when(departmentRepositoryMock.findByNameAndBatchEntity(any(String.class), any(BatchEntity.class))).thenReturn(Optional.empty());
		when(departmentRepositoryMock.save(any(DepartmentEntity.class))).thenReturn(DepartmentEntity.builder().build());
		when(requestRepositoryMock.save(any(RequestEntity.class))).thenReturn(RequestEntity.builder().build());

		//Act
		snailMailService.sendSnailMail(municipalityId, request);

		//Assert
		verify(citizenIntegrationMock).getCitizen(any(String.class));
		verify(batchRepositoryMock).findByMunicipalityIdAndId(municipalityId, request.getBatchId());
		verify(departmentRepositoryMock).findByNameAndBatchEntity(anyString(), any(BatchEntity.class));
		verify(departmentRepositoryMock).save(any(DepartmentEntity.class));
		verify(requestRepositoryMock).save(any(RequestEntity.class));
		verifyNoMoreInteractions(batchRepositoryMock, departmentRepositoryMock, requestRepositoryMock, citizenIntegrationMock);
		verifyNoInteractions(sambaIntegrationMock);

	}

	@Test
	void sendMailWithNewBatchAndDepartment() {

		// Arrange
		final var municipalityId = "municipalityId";
		final var request = buildSendSnailMailRequest();

		when(citizenIntegrationMock.getCitizen(any(String.class))).thenReturn(buildCitizenExtended());
		when(batchRepositoryMock.findByMunicipalityIdAndId(municipalityId, request.getBatchId())).thenReturn(Optional.empty());
		when(batchRepositoryMock.save(any(BatchEntity.class))).thenReturn(BatchEntity.builder().build());
		when(departmentRepositoryMock.findByNameAndBatchEntity(any(String.class), any(BatchEntity.class))).thenReturn(Optional.empty());
		when(departmentRepositoryMock.save(any(DepartmentEntity.class))).thenReturn(DepartmentEntity.builder().build());

		// Act
		snailMailService.sendSnailMail(municipalityId, request);

		// Assert
		verify(batchRepositoryMock).findByMunicipalityIdAndId(municipalityId, request.getBatchId());
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

		// Arrange
		final var municipalityId = "municipalityId";
		final var request = buildSendSnailMailRequest();
		when(citizenIntegrationMock.getCitizen(any(String.class))).thenReturn(null);
		when(batchRepositoryMock.findByMunicipalityIdAndId(municipalityId, request.getBatchId())).thenReturn(Optional.empty());
		when(batchRepositoryMock.save(any(BatchEntity.class))).thenReturn(BatchEntity.builder().build());
		when(departmentRepositoryMock.findByNameAndBatchEntity(any(String.class), any(BatchEntity.class))).thenReturn(Optional.empty());
		when(departmentRepositoryMock.save(any(DepartmentEntity.class))).thenReturn(DepartmentEntity.builder().build());

		// Act
		snailMailService.sendSnailMail(municipalityId, request);

		// Assert
		verify(batchRepositoryMock).findByMunicipalityIdAndId(municipalityId, request.getBatchId());
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
		// Arrange
		final var batchEntity = BatchEntity.builder().build();
		final var batchId = "batchId";
		final var municipalityId = "municipalityId";

		when(batchRepositoryMock.findByMunicipalityIdAndId(municipalityId, batchId)).thenReturn(Optional.ofNullable(batchEntity));

		// Act
		snailMailService.sendBatch(municipalityId, batchId);

		// Assert
		verify(batchRepositoryMock).findByMunicipalityIdAndId(municipalityId, batchId);
		verify(sambaIntegrationMock).writeBatchDataToSambaShare(any(BatchEntity.class));
		verify(batchRepositoryMock).delete(any(BatchEntity.class));
		verifyNoMoreInteractions(batchRepositoryMock, sambaIntegrationMock);
		verifyNoInteractions(departmentRepositoryMock, requestRepositoryMock, citizenIntegrationMock);
	}

	@Test
	void sendBatchWithNoBatchFound() {

		// Arrange
		final var batchId = "batchId";
		final var municipalityId = "municipalityId";

		when(batchRepositoryMock.findByMunicipalityIdAndId(municipalityId, batchId)).thenReturn(Optional.empty());

		// Act
		assertThatThrownBy(() -> snailMailService.sendBatch(municipalityId, batchId))
			.isInstanceOf(Problem.class)
			.hasMessage("No batch found: Failed to fetch batch data from database");

		// Assert
		verify(batchRepositoryMock).findByMunicipalityIdAndId(municipalityId, batchId);
		verifyNoMoreInteractions(batchRepositoryMock);
		verifyNoInteractions(departmentRepositoryMock, sambaIntegrationMock, requestRepositoryMock, citizenIntegrationMock);
	}

	@Test
	void sendBatchWithoutEvelopeType() {
		// Arrange
		final var batchEntity = BatchEntity.builder().build();
		final var request = buildSendSnailMailRequest();
		final var municipalityId = "municipalityId";
		request.getAttachments().getFirst().setEnvelopeType(null);

		// Mock
		when(batchRepositoryMock.findByMunicipalityIdAndId(municipalityId, request.getBatchId())).thenReturn(Optional.ofNullable(batchEntity));
		when(departmentRepositoryMock.findByNameAndBatchEntity(any(String.class), any(BatchEntity.class))).thenReturn(Optional.ofNullable(DepartmentEntity.builder().build()));
		when(citizenIntegrationMock.getCitizen(any(String.class))).thenReturn(buildCitizenExtended());

		// Act
		snailMailService.sendSnailMail(municipalityId, request);

		// Assert
		verify(batchRepositoryMock).findByMunicipalityIdAndId(municipalityId, request.getBatchId());
		verify(departmentRepositoryMock).findByNameAndBatchEntity(any(String.class), any(BatchEntity.class));
		verify(requestRepositoryMock).save(any());
		verify(citizenIntegrationMock).getCitizen(any(String.class));
		verifyNoMoreInteractions(batchRepositoryMock, departmentRepositoryMock, requestRepositoryMock, citizenIntegrationMock);
		verifyNoInteractions(sambaIntegrationMock);

	}

}
