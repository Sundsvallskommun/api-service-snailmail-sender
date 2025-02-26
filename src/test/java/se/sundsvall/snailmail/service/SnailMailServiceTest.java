package se.sundsvall.snailmail.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.snailmail.TestDataFactory.buildSendSnailMailAddress;
import static se.sundsvall.snailmail.TestDataFactory.buildSendSnailMailRequest;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.zalando.problem.Problem;
import se.sundsvall.snailmail.integration.db.BatchRepository;
import se.sundsvall.snailmail.integration.db.DepartmentRepository;
import se.sundsvall.snailmail.integration.db.RequestRepository;
import se.sundsvall.snailmail.integration.db.model.BatchEntity;
import se.sundsvall.snailmail.integration.db.model.DepartmentEntity;
import se.sundsvall.snailmail.integration.db.model.RequestEntity;
import se.sundsvall.snailmail.integration.samba.SambaIntegration;
import se.sundsvall.snailmail.integration.sftp.SftpIntegration;

@ExtendWith(MockitoExtension.class)
class SnailMailServiceTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String BATCH_ID = "batchId";

	@Mock
	private BatchRepository batchRepositoryMock;

	@Mock
	private DepartmentRepository departmentRepositoryMock;

	@Mock
	private RequestRepository requestRepositoryMock;

	@Mock
	private SambaIntegration sambaIntegrationMock;

	@Mock
	private SftpIntegration sftpIntegrationMock;

	@Captor
	private ArgumentCaptor<RequestEntity> requestEntityArgumentCaptor;

	@InjectMocks
	private SnailMailService snailMailService;

	@Test
	void sendMail() {
		var batchEntity = BatchEntity.builder().withId(BATCH_ID).build();
		var request = buildSendSnailMailRequest();
		when(batchRepositoryMock.findByMunicipalityIdAndId(MUNICIPALITY_ID, request.getBatchId())).thenReturn(Optional.ofNullable(batchEntity));
		when(departmentRepositoryMock.findByNameAndBatchEntityId(any(String.class), anyString())).thenReturn(Optional.ofNullable(DepartmentEntity.builder().build()));

		snailMailService.sendSnailMail(request);

		verify(batchRepositoryMock).findByMunicipalityIdAndId(MUNICIPALITY_ID, request.getBatchId());
		verify(departmentRepositoryMock).findByNameAndBatchEntityId("someDepartment", batchEntity.getId());
		verify(requestRepositoryMock).save(any(RequestEntity.class));
		verifyNoMoreInteractions(batchRepositoryMock, departmentRepositoryMock, requestRepositoryMock);
		verifyNoInteractions(sambaIntegrationMock);
	}

	@Test
	void sendMailWithNewBatch() {
		var batchEntity = BatchEntity.builder().withId(BATCH_ID).build();
		var departmentEntity = DepartmentEntity.builder().build();
		var request = buildSendSnailMailRequest();

		when(batchRepositoryMock.findByMunicipalityIdAndId(MUNICIPALITY_ID, request.getBatchId())).thenReturn(Optional.empty());
		when(batchRepositoryMock.save(any(BatchEntity.class))).thenReturn(batchEntity);
		when(departmentRepositoryMock.findByNameAndBatchEntityId(anyString(), anyString())).thenReturn(Optional.empty());
		when(departmentRepositoryMock.save(any(DepartmentEntity.class))).thenReturn(departmentEntity);
		when(requestRepositoryMock.save(any(RequestEntity.class))).thenReturn(RequestEntity.builder().build());

		snailMailService.sendSnailMail(request);

		verify(batchRepositoryMock).findByMunicipalityIdAndId(MUNICIPALITY_ID, request.getBatchId());
		verify(batchRepositoryMock).save(any(BatchEntity.class));
		verify(departmentRepositoryMock).findByNameAndBatchEntityId("someDepartment", batchEntity.getId());
		verify(departmentRepositoryMock).save(any(DepartmentEntity.class));
		verify(requestRepositoryMock).save(any(RequestEntity.class));
		verifyNoMoreInteractions(batchRepositoryMock, departmentRepositoryMock, requestRepositoryMock);
		verifyNoInteractions(sambaIntegrationMock);
	}

	@Test
	void sendMailWithNewDepartment() {
		var batchEntity = BatchEntity.builder().build();
		var request = buildSendSnailMailRequest();

		when(batchRepositoryMock.findByMunicipalityIdAndId(MUNICIPALITY_ID, request.getBatchId())).thenReturn(Optional.ofNullable(batchEntity));
		when(departmentRepositoryMock.findByNameAndBatchEntityId(any(String.class), any())).thenReturn(Optional.empty());
		when(departmentRepositoryMock.save(any(DepartmentEntity.class))).thenReturn(DepartmentEntity.builder().build());
		when(requestRepositoryMock.save(any(RequestEntity.class))).thenReturn(RequestEntity.builder().build());

		snailMailService.sendSnailMail(request);

		verify(batchRepositoryMock).findByMunicipalityIdAndId(MUNICIPALITY_ID, request.getBatchId());
		verify(departmentRepositoryMock).findByNameAndBatchEntityId(anyString(), any());
		verify(departmentRepositoryMock).save(any(DepartmentEntity.class));
		verify(requestRepositoryMock).save(any(RequestEntity.class));
		verifyNoMoreInteractions(batchRepositoryMock, departmentRepositoryMock, requestRepositoryMock);
		verifyNoInteractions(sambaIntegrationMock);
	}

	@Test
	void sendMailWithNewBatchAndDepartment() {
		var request = buildSendSnailMailRequest();

		when(batchRepositoryMock.findByMunicipalityIdAndId(MUNICIPALITY_ID, request.getBatchId())).thenReturn(Optional.empty());
		when(batchRepositoryMock.save(any(BatchEntity.class))).thenReturn(BatchEntity.builder().build());
		when(departmentRepositoryMock.findByNameAndBatchEntityId(any(String.class), any())).thenReturn(Optional.empty());
		when(departmentRepositoryMock.save(any(DepartmentEntity.class))).thenReturn(DepartmentEntity.builder().build());

		snailMailService.sendSnailMail(request);

		verify(batchRepositoryMock).findByMunicipalityIdAndId(MUNICIPALITY_ID, request.getBatchId());
		verify(batchRepositoryMock).save(any(BatchEntity.class));
		verify(departmentRepositoryMock).findByNameAndBatchEntityId(anyString(), any());
		verify(departmentRepositoryMock).save(any(DepartmentEntity.class));
		verify(requestRepositoryMock).save(any(RequestEntity.class));
		verifyNoMoreInteractions(batchRepositoryMock, departmentRepositoryMock, requestRepositoryMock);
		verifyNoInteractions(sambaIntegrationMock);
	}

	@Test
	void sendMailWithNewBatchAndDepartmentAndCitizen() {
		var request = buildSendSnailMailRequest();

		when(batchRepositoryMock.findByMunicipalityIdAndId(MUNICIPALITY_ID, request.getBatchId())).thenReturn(Optional.empty());
		when(batchRepositoryMock.save(any(BatchEntity.class))).thenReturn(BatchEntity.builder().build());
		when(departmentRepositoryMock.findByNameAndBatchEntityId(any(String.class), any())).thenReturn(Optional.empty());
		when(departmentRepositoryMock.save(any(DepartmentEntity.class))).thenReturn(DepartmentEntity.builder().build());

		snailMailService.sendSnailMail(request);

		verify(batchRepositoryMock).findByMunicipalityIdAndId(MUNICIPALITY_ID, request.getBatchId());
		verify(batchRepositoryMock).save(any(BatchEntity.class));
		verify(departmentRepositoryMock).findByNameAndBatchEntityId(any(String.class), any());
		verify(departmentRepositoryMock).save(any(DepartmentEntity.class));
		verify(requestRepositoryMock).save(any(RequestEntity.class));
		verifyNoMoreInteractions(batchRepositoryMock, departmentRepositoryMock, requestRepositoryMock);
		verifyNoInteractions(sambaIntegrationMock);
	}

	@Test
	void sendMailWithNewBatchAndDepartmentWithAddress() {
		var request = buildSendSnailMailRequest().withAddress(buildSendSnailMailAddress());

		when(batchRepositoryMock.findByMunicipalityIdAndId(MUNICIPALITY_ID, request.getBatchId())).thenReturn(Optional.empty());
		when(batchRepositoryMock.save(any(BatchEntity.class))).thenReturn(BatchEntity.builder().build());
		when(departmentRepositoryMock.findByNameAndBatchEntityId(any(String.class), any())).thenReturn(Optional.empty());
		when(departmentRepositoryMock.save(any(DepartmentEntity.class))).thenReturn(DepartmentEntity.builder().build());

		snailMailService.sendSnailMail(request);

		verify(batchRepositoryMock).findByMunicipalityIdAndId(MUNICIPALITY_ID, request.getBatchId());
		verify(batchRepositoryMock).save(any(BatchEntity.class));
		verify(departmentRepositoryMock).findByNameAndBatchEntityId(any(String.class), any());
		verify(departmentRepositoryMock).save(any(DepartmentEntity.class));
		verify(requestRepositoryMock).save(requestEntityArgumentCaptor.capture());
		var requestEntity = requestEntityArgumentCaptor.getValue();
		assertThat(requestEntity.getRecipientEntity()).satisfies(recipientEntity -> {
			assertThat(recipientEntity.getGivenName()).isEqualTo(request.getAddress().getFirstName());
			assertThat(recipientEntity.getLastName()).isEqualTo(request.getAddress().getLastName());
			assertThat(recipientEntity.getAddress()).isEqualTo(request.getAddress().getAddress());
			assertThat(recipientEntity.getApartmentNumber()).isEqualTo(request.getAddress().getApartmentNumber());
			assertThat(recipientEntity.getPostalCode()).isEqualTo(request.getAddress().getZipCode());
			assertThat(recipientEntity.getCity()).isEqualTo(request.getAddress().getCity());
			assertThat(recipientEntity.getCareOf()).isEqualTo(request.getAddress().getCareOf());
		});
		verifyNoMoreInteractions(batchRepositoryMock, departmentRepositoryMock, requestRepositoryMock);
		verifyNoInteractions(sambaIntegrationMock);
	}

	/**
	 * Tests the scenario where sambaActive is true and sftpActive is false
	 */
	@Test
	void sendBatch_1() {
		var batchEntity = BatchEntity.builder().build();

		when(batchRepositoryMock.findByMunicipalityIdAndId(MUNICIPALITY_ID, BATCH_ID)).thenReturn(Optional.ofNullable(batchEntity));
		ReflectionTestUtils.setField(snailMailService, "sambaActive", true);
		ReflectionTestUtils.setField(snailMailService, "sftpActive", false);

		snailMailService.sendBatch(MUNICIPALITY_ID, BATCH_ID);

		verify(batchRepositoryMock).findByMunicipalityIdAndId(MUNICIPALITY_ID, BATCH_ID);
		verify(sambaIntegrationMock).writeBatchDataToSambaShare(any(BatchEntity.class));
		verify(batchRepositoryMock).delete(any(BatchEntity.class));
		verifyNoMoreInteractions(batchRepositoryMock, sambaIntegrationMock);
		verifyNoInteractions(departmentRepositoryMock, requestRepositoryMock);
	}

	/**
	 * Tests the scenario where sambaActive is false and sftpActive is true
	 */
	@Test
	void sendBatch_2() {
		var batchEntity = BatchEntity.builder().build();

		when(batchRepositoryMock.findByMunicipalityIdAndId(MUNICIPALITY_ID, BATCH_ID)).thenReturn(Optional.ofNullable(batchEntity));
		ReflectionTestUtils.setField(snailMailService, "sambaActive", false);
		ReflectionTestUtils.setField(snailMailService, "sftpActive", true);

		snailMailService.sendBatch(MUNICIPALITY_ID, BATCH_ID);

		verify(batchRepositoryMock).findByMunicipalityIdAndId(MUNICIPALITY_ID, BATCH_ID);
		verify(sftpIntegrationMock).writeBatchDataToSftp(any(BatchEntity.class));
		verify(batchRepositoryMock).delete(any(BatchEntity.class));
		verifyNoMoreInteractions(batchRepositoryMock, sambaIntegrationMock);
		verifyNoInteractions(departmentRepositoryMock, requestRepositoryMock);
	}

	/**
	 * Tests the scenario where sambaActive is false and sftpActive is false
	 */
	@Test
	void sendBatch_3() {
		var batchEntity = BatchEntity.builder().build();

		when(batchRepositoryMock.findByMunicipalityIdAndId(MUNICIPALITY_ID, BATCH_ID)).thenReturn(Optional.ofNullable(batchEntity));
		ReflectionTestUtils.setField(snailMailService, "sambaActive", false);
		ReflectionTestUtils.setField(snailMailService, "sftpActive", false);

		assertThatThrownBy(() -> snailMailService.sendBatch(MUNICIPALITY_ID, BATCH_ID))
			.isInstanceOf(Problem.class)
			.hasMessage("Internal Server Error: No integration active");

		verify(batchRepositoryMock).findByMunicipalityIdAndId(MUNICIPALITY_ID, BATCH_ID);
		verify(batchRepositoryMock, never()).delete(any(BatchEntity.class));
		verifyNoMoreInteractions(batchRepositoryMock);
		verifyNoInteractions(departmentRepositoryMock, requestRepositoryMock, sambaIntegrationMock);
	}

	/**
	 * Tests the scenario where sambaActive is true and sftpActive is true
	 */
	@Test
	void sendBatch_4() {
		var batchEntity = BatchEntity.builder().build();

		when(batchRepositoryMock.findByMunicipalityIdAndId(MUNICIPALITY_ID, BATCH_ID)).thenReturn(Optional.ofNullable(batchEntity));
		ReflectionTestUtils.setField(snailMailService, "sambaActive", true);
		ReflectionTestUtils.setField(snailMailService, "sftpActive", true);

		snailMailService.sendBatch(MUNICIPALITY_ID, BATCH_ID);

		verify(batchRepositoryMock).findByMunicipalityIdAndId(MUNICIPALITY_ID, BATCH_ID);
		verify(batchRepositoryMock).delete(any(BatchEntity.class));
		verify(sftpIntegrationMock).writeBatchDataToSftp(any(BatchEntity.class));
		verify(sambaIntegrationMock).writeBatchDataToSambaShare(any(BatchEntity.class));
		verifyNoMoreInteractions(batchRepositoryMock, sftpIntegrationMock, sambaIntegrationMock);
		verifyNoInteractions(departmentRepositoryMock, requestRepositoryMock);
	}

	/**
	 * Tests the scenario where sambaActive is false and sftpActive is true
	 */
	@Test
	void sendBatch_2() {
		var batchEntity = BatchEntity.builder().build();

		when(batchRepositoryMock.findByMunicipalityIdAndId(MUNICIPALITY_ID, BATCH_ID)).thenReturn(Optional.ofNullable(batchEntity));
		ReflectionTestUtils.setField(snailMailService, "sambaActive", false);
		ReflectionTestUtils.setField(snailMailService, "sftpActive", true);

		snailMailService.sendBatch(MUNICIPALITY_ID, BATCH_ID);

		verify(batchRepositoryMock).findByMunicipalityIdAndId(MUNICIPALITY_ID, BATCH_ID);
		verify(sftpIntegrationMock).writeBatchDataToSftp(any(BatchEntity.class));
		verify(batchRepositoryMock).delete(any(BatchEntity.class));
		verifyNoMoreInteractions(batchRepositoryMock, sambaIntegrationMock);
		verifyNoInteractions(departmentRepositoryMock, requestRepositoryMock);
	}

	/**
	 * Tests the scenario where sambaActive is false and sftpActive is false
	 */
	@Test
	void sendBatch_3() {
		var batchEntity = BatchEntity.builder().build();

		when(batchRepositoryMock.findByMunicipalityIdAndId(MUNICIPALITY_ID, BATCH_ID)).thenReturn(Optional.ofNullable(batchEntity));
		ReflectionTestUtils.setField(snailMailService, "sambaActive", false);
		ReflectionTestUtils.setField(snailMailService, "sftpActive", false);

		assertThatThrownBy(() -> snailMailService.sendBatch(MUNICIPALITY_ID, BATCH_ID))
			.isInstanceOf(Problem.class)
			.hasMessage("Internal Server Error: No integration active");

		verify(batchRepositoryMock).findByMunicipalityIdAndId(MUNICIPALITY_ID, BATCH_ID);
		verify(batchRepositoryMock, never()).delete(any(BatchEntity.class));
		verifyNoMoreInteractions(batchRepositoryMock);
		verifyNoInteractions(departmentRepositoryMock, requestRepositoryMock, sambaIntegrationMock);
	}

	/**
	 * Tests the scenario where sambaActive is true and sftpActive is true
	 */
	@Test
	void sendBatch_4() {
		var batchEntity = BatchEntity.builder().build();

		when(batchRepositoryMock.findByMunicipalityIdAndId(MUNICIPALITY_ID, BATCH_ID)).thenReturn(Optional.ofNullable(batchEntity));
		ReflectionTestUtils.setField(snailMailService, "sambaActive", true);
		ReflectionTestUtils.setField(snailMailService, "sftpActive", true);

		snailMailService.sendBatch(MUNICIPALITY_ID, BATCH_ID);

		verify(batchRepositoryMock).findByMunicipalityIdAndId(MUNICIPALITY_ID, BATCH_ID);
		verify(batchRepositoryMock).delete(any(BatchEntity.class));
		verify(sftpIntegrationMock).writeBatchDataToSftp(any(BatchEntity.class));
		verify(sambaIntegrationMock).writeBatchDataToSambaShare(any(BatchEntity.class));
		verifyNoMoreInteractions(batchRepositoryMock, sftpIntegrationMock, sambaIntegrationMock);
		verifyNoInteractions(departmentRepositoryMock, requestRepositoryMock);
	}

	@Test
	void sendBatchWithNoBatchFound() {
		when(batchRepositoryMock.findByMunicipalityIdAndId(MUNICIPALITY_ID, BATCH_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> snailMailService.sendBatch(MUNICIPALITY_ID, BATCH_ID))
			.isInstanceOf(Problem.class)
			.hasMessage("No batch found: Failed to fetch batch data from database");

		verify(batchRepositoryMock).findByMunicipalityIdAndId(MUNICIPALITY_ID, BATCH_ID);
		verifyNoMoreInteractions(batchRepositoryMock);
		verifyNoInteractions(departmentRepositoryMock, sambaIntegrationMock, requestRepositoryMock);
	}

	@Test
	void sendBatchWithoutEnvelopeType() {
		var batchEntity = BatchEntity.builder().withId(BATCH_ID).build();
		var request = buildSendSnailMailRequest();
		request.getAttachments().getFirst().setEnvelopeType(null);

		when(batchRepositoryMock.findByMunicipalityIdAndId(MUNICIPALITY_ID, request.getBatchId())).thenReturn(Optional.ofNullable(batchEntity));
		when(departmentRepositoryMock.findByNameAndBatchEntityId(any(String.class), anyString())).thenReturn(Optional.ofNullable(DepartmentEntity.builder().build()));

		snailMailService.sendSnailMail(request);

		verify(batchRepositoryMock).findByMunicipalityIdAndId(MUNICIPALITY_ID, request.getBatchId());
		verify(departmentRepositoryMock).findByNameAndBatchEntityId(any(String.class), anyString());
		verify(requestRepositoryMock).save(any());
		verifyNoMoreInteractions(batchRepositoryMock, departmentRepositoryMock, requestRepositoryMock);
		verifyNoInteractions(sambaIntegrationMock);
	}
}
