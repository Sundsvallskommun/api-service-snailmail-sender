package se.sundsvall.snailmail.service;

import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.zalando.problem.Problem;
import se.sundsvall.snailmail.integration.db.RequestRepository;
import se.sundsvall.snailmail.integration.db.model.BatchEntity;
import se.sundsvall.snailmail.integration.db.model.DepartmentEntity;
import se.sundsvall.snailmail.integration.db.model.RequestEntity;
import se.sundsvall.snailmail.integration.samba.SambaIntegration;
import se.sundsvall.snailmail.integration.sftp.SftpIntegration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.snailmail.TestDataFactory.buildSendSnailMailAddress;
import static se.sundsvall.snailmail.TestDataFactory.buildSendSnailMailRequest;

@ExtendWith(MockitoExtension.class)
class SnailMailServiceTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String BATCH_ID = "batchId";

	@Mock
	private BatchService batchServiceMock;

	@Mock
	private DepartmentService departmentServiceMock;

	@Mock
	private RequestRepository requestRepositoryMock;

	@Mock
	private SambaIntegration sambaIntegrationMock;

	@Mock
	private SftpIntegration sftpIntegrationMock;

	@Mock
	private Semaphore semaphoreMock;

	@Captor
	private ArgumentCaptor<RequestEntity> requestEntityArgumentCaptor;

	@InjectMocks
	private SnailMailService snailMailService;

	@Test
	void sendMail() throws InterruptedException {
		var batchEntity = BatchEntity.builder().withId(BATCH_ID).build();
		var request = buildSendSnailMailRequest();
		when(semaphoreMock.tryAcquire(10L, TimeUnit.SECONDS)).thenReturn(true);
		when(batchServiceMock.getOrCreateBatch(request)).thenReturn(batchEntity);
		when(departmentServiceMock.getOrCreateDepartment(request.getDepartment(), request.getFolderName(), batchEntity)).thenReturn(DepartmentEntity.builder().build());

		snailMailService.sendSnailMail(request);

		verify(semaphoreMock).tryAcquire(10L, TimeUnit.SECONDS);
		verify(batchServiceMock).getOrCreateBatch(request);
		verify(departmentServiceMock).getOrCreateDepartment("someDepartment", "someFolder", batchEntity);
		verify(requestRepositoryMock).save(any(RequestEntity.class));
		verify(semaphoreMock).release();
		verifyNoMoreInteractions(batchServiceMock, departmentServiceMock, requestRepositoryMock, semaphoreMock);
		verifyNoInteractions(sambaIntegrationMock);
	}

	@Test
	void sendBatchWithNoBatchFound() {
		when(batchServiceMock.findBatchByMunicipalityIdAndId(MUNICIPALITY_ID, BATCH_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> snailMailService.sendBatch(MUNICIPALITY_ID, BATCH_ID))
			.isInstanceOf(Problem.class)
			.hasMessage("No batch found: Failed to fetch batch data from database");

		verify(batchServiceMock).findBatchByMunicipalityIdAndId(MUNICIPALITY_ID, BATCH_ID);
		verifyNoMoreInteractions(batchServiceMock);
		verifyNoInteractions(departmentServiceMock, sambaIntegrationMock, requestRepositoryMock);
	}

	@Test
	void sendMailWithNewBatchAndDepartmentWithAddress() throws InterruptedException {
		var request = buildSendSnailMailRequest().withAddress(buildSendSnailMailAddress());
		var batch = BatchEntity.builder().withId(BATCH_ID).build();
		var department = DepartmentEntity.builder().withName(request.getDepartment()).build();

		when(semaphoreMock.tryAcquire(10L, TimeUnit.SECONDS)).thenReturn(true);
		when(batchServiceMock.getOrCreateBatch(request)).thenReturn(batch);
		when(departmentServiceMock.getOrCreateDepartment(request.getDepartment(), request.getFolderName(), batch)).thenReturn(department);

		snailMailService.sendSnailMail(request);

		verify(semaphoreMock).tryAcquire(10L, TimeUnit.SECONDS);
		verify(batchServiceMock).getOrCreateBatch(request);
		verify(departmentServiceMock).getOrCreateDepartment(request.getDepartment(), request.getFolderName(), batch);
		verify(requestRepositoryMock).save(requestEntityArgumentCaptor.capture());
		verify(semaphoreMock).release();

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

		verifyNoMoreInteractions(batchServiceMock, departmentServiceMock, requestRepositoryMock, semaphoreMock);
		verifyNoInteractions(sambaIntegrationMock);
	}

	@Test
	void sendBatchWithSemaphoreFailure() throws InterruptedException {
		var request = buildSendSnailMailRequest();

		when(semaphoreMock.tryAcquire(10L, TimeUnit.SECONDS)).thenReturn(false);

		assertThatThrownBy(() -> snailMailService.sendSnailMail(request))
			.isInstanceOf(Problem.class)
			.hasMessage("Internal Server Error: Couldn't acquire lock for sending snail mail request");

		verify(semaphoreMock).tryAcquire(10L, TimeUnit.SECONDS);
		verify(semaphoreMock).release();
		verifyNoMoreInteractions(semaphoreMock);
		verifyNoInteractions(batchServiceMock, departmentServiceMock, requestRepositoryMock, sambaIntegrationMock);
	}

	@Test
	void sendBatch_sambaOnly() {
		var batch = BatchEntity.builder().build();
		when(batchServiceMock.findBatchByMunicipalityIdAndId(MUNICIPALITY_ID, BATCH_ID)).thenReturn(Optional.of(batch));
		ReflectionTestUtils.setField(snailMailService, "sambaActive", true);
		ReflectionTestUtils.setField(snailMailService, "sftpActive", false);

		snailMailService.sendBatch(MUNICIPALITY_ID, BATCH_ID);

		verify(batchServiceMock).findBatchByMunicipalityIdAndId(MUNICIPALITY_ID, BATCH_ID);
		verify(sambaIntegrationMock).writeBatchDataToSambaShare(batch);
		verify(batchServiceMock).deleteBatch(batch);
		verifyNoMoreInteractions(batchServiceMock, sambaIntegrationMock);
		verifyNoInteractions(departmentServiceMock, requestRepositoryMock);
	}

	@Test
	void sendBatch_sftpOnly() {
		var batch = BatchEntity.builder().build();
		when(batchServiceMock.findBatchByMunicipalityIdAndId(MUNICIPALITY_ID, BATCH_ID)).thenReturn(Optional.of(batch));
		ReflectionTestUtils.setField(snailMailService, "sambaActive", false);
		ReflectionTestUtils.setField(snailMailService, "sftpActive", true);

		snailMailService.sendBatch(MUNICIPALITY_ID, BATCH_ID);

		verify(batchServiceMock).findBatchByMunicipalityIdAndId(MUNICIPALITY_ID, BATCH_ID);
		verify(sftpIntegrationMock).writeBatchDataToSftp(batch);
		verify(batchServiceMock).deleteBatch(batch);
		verifyNoMoreInteractions(batchServiceMock, sftpIntegrationMock);
		verifyNoInteractions(departmentServiceMock, requestRepositoryMock);
	}

	@Test
	void sendBatch_bothIntegrations() {
		var batch = BatchEntity.builder().build();
		when(batchServiceMock.findBatchByMunicipalityIdAndId(MUNICIPALITY_ID, BATCH_ID)).thenReturn(Optional.of(batch));
		ReflectionTestUtils.setField(snailMailService, "sambaActive", true);
		ReflectionTestUtils.setField(snailMailService, "sftpActive", true);

		snailMailService.sendBatch(MUNICIPALITY_ID, BATCH_ID);

		verify(batchServiceMock).findBatchByMunicipalityIdAndId(MUNICIPALITY_ID, BATCH_ID);
		verify(sambaIntegrationMock).writeBatchDataToSambaShare(batch);
		verify(sftpIntegrationMock).writeBatchDataToSftp(batch);
		verify(batchServiceMock).deleteBatch(batch);
		verifyNoMoreInteractions(batchServiceMock, sambaIntegrationMock, sftpIntegrationMock);
		verifyNoInteractions(departmentServiceMock, requestRepositoryMock);
	}

	@Test
	void sendBatch_noIntegrationActive() {
		var batch = BatchEntity.builder().build();
		when(batchServiceMock.findBatchByMunicipalityIdAndId(MUNICIPALITY_ID, BATCH_ID)).thenReturn(Optional.of(batch));
		ReflectionTestUtils.setField(snailMailService, "sambaActive", false);
		ReflectionTestUtils.setField(snailMailService, "sftpActive", false);

		assertThatThrownBy(() -> snailMailService.sendBatch(MUNICIPALITY_ID, BATCH_ID))
			.isInstanceOf(Problem.class)
			.hasMessage("Internal Server Error: No integration active");

		verify(batchServiceMock).findBatchByMunicipalityIdAndId(MUNICIPALITY_ID, BATCH_ID);
		verify(batchServiceMock, never()).deleteBatch(batch);
		verifyNoMoreInteractions(batchServiceMock);
		verifyNoInteractions(departmentServiceMock, requestRepositoryMock, sambaIntegrationMock, sftpIntegrationMock);
	}
}
