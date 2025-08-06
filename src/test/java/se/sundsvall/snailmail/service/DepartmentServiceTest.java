package se.sundsvall.snailmail.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.snailmail.integration.db.DepartmentRepository;
import se.sundsvall.snailmail.integration.db.model.BatchEntity;
import se.sundsvall.snailmail.integration.db.model.DepartmentEntity;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

	private static final String DEPARTMENT_NAME = "dept44";
	private static final String BATCH_ID = "123";

	@Mock
	private DepartmentRepository departmentRepositoryMock;

	@InjectMocks
	private DepartmentService departmentService;

	@Test
	void getOrCreateDepartment_shouldReturnExisting() {
		final var batchEntity = BatchEntity.builder()
			.withId(BATCH_ID)
			.build();
		final var expectedEntity = DepartmentEntity.builder()
			.withName(DEPARTMENT_NAME)
			.withBatchEntity(batchEntity)
			.build();

		when(departmentRepositoryMock.findByNameAndBatchEntityId(DEPARTMENT_NAME, BATCH_ID))
			.thenReturn(Optional.of(expectedEntity));

		final var result = departmentService.getOrCreateDepartment(DEPARTMENT_NAME, batchEntity);
		assertThat(result.getName()).isEqualTo(DEPARTMENT_NAME);
		assertThat(result.getBatchEntity()).isEqualTo(batchEntity);

		verify(departmentRepositoryMock, never()).save(any());
	}

	@Test
	void getOrCreateDepartment_shouldCreateNewIfNotFound() {
		final var batchEntity = BatchEntity.builder()
			.withId(BATCH_ID)
			.build();
		final var expectedEntity = DepartmentEntity.builder()
			.withName(DEPARTMENT_NAME)
			.withBatchEntity(batchEntity)
			.build();

		when(departmentRepositoryMock.findByNameAndBatchEntityId(DEPARTMENT_NAME, BATCH_ID))
			.thenReturn(Optional.empty());
		when(departmentRepositoryMock.save(any(DepartmentEntity.class))).thenReturn(expectedEntity);

		final var result = departmentService.getOrCreateDepartment(DEPARTMENT_NAME, batchEntity);
		assertThat(result.getName()).isEqualTo(DEPARTMENT_NAME);

		final var captor = ArgumentCaptor.forClass(DepartmentEntity.class);
		verify(departmentRepositoryMock).save(captor.capture());

		final var value = captor.getValue();
		assertThat(value.getName()).isEqualTo(DEPARTMENT_NAME);
		assertThat(value.getBatchEntity()).isEqualTo(batchEntity);
	}
}
