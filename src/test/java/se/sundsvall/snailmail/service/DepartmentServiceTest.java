package se.sundsvall.snailmail.service;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.snailmail.integration.db.DepartmentRepository;
import se.sundsvall.snailmail.integration.db.model.BatchEntity;
import se.sundsvall.snailmail.integration.db.model.DepartmentEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

	private static final String DEPARTMENT_NAME = "dept44";
	private static final String FOLDER_NAME = "Sundsvalls Kommun";
	private static final String BATCH_ID = "123";

	@Mock
	private DepartmentRepository departmentRepositoryMock;

	@Mock
	private DepartmentCreator departmentCreatorMock;

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

		when(departmentRepositoryMock.findByNameAndFolderNameAndBatchEntityId(DEPARTMENT_NAME, FOLDER_NAME, BATCH_ID))
			.thenReturn(Optional.of(expectedEntity));

		final var result = departmentService.getOrCreateDepartment(DEPARTMENT_NAME, FOLDER_NAME, batchEntity);
		assertThat(result.getName()).isEqualTo(DEPARTMENT_NAME);
		assertThat(result.getBatchEntity()).isEqualTo(batchEntity);

		verify(departmentRepositoryMock).findByNameAndFolderNameAndBatchEntityId(DEPARTMENT_NAME, FOLDER_NAME, BATCH_ID);
		verify(departmentCreatorMock, never()).save(any());
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

		when(departmentRepositoryMock.findByNameAndFolderNameAndBatchEntityId(DEPARTMENT_NAME, FOLDER_NAME, BATCH_ID))
			.thenReturn(Optional.empty());
		when(departmentCreatorMock.save(any(DepartmentEntity.class))).thenReturn(expectedEntity);

		final var result = departmentService.getOrCreateDepartment(DEPARTMENT_NAME, FOLDER_NAME, batchEntity);
		assertThat(result.getName()).isEqualTo(DEPARTMENT_NAME);

		final var captor = ArgumentCaptor.forClass(DepartmentEntity.class);
		verify(departmentCreatorMock).save(captor.capture());

		final var value = captor.getValue();
		assertThat(value.getName()).isEqualTo(DEPARTMENT_NAME);
		assertThat(value.getBatchEntity()).isEqualTo(batchEntity);
	}

	@Test
	void getOrCreateDepartment_shouldNormalizeBlankFolderNameToEmpty() {
		final var batchEntity = BatchEntity.builder()
			.withId(BATCH_ID)
			.build();
		final var expectedEntity = DepartmentEntity.builder()
			.withName(DEPARTMENT_NAME)
			.withFolderName("")
			.withBatchEntity(batchEntity)
			.build();

		when(departmentRepositoryMock.findByNameAndFolderNameAndBatchEntityId(DEPARTMENT_NAME, "", BATCH_ID))
			.thenReturn(Optional.empty());
		when(departmentCreatorMock.save(any(DepartmentEntity.class))).thenReturn(expectedEntity);

		final var result = departmentService.getOrCreateDepartment(DEPARTMENT_NAME, null, batchEntity);
		assertThat(result.getFolderName()).isEqualTo("");

		final var captor = ArgumentCaptor.forClass(DepartmentEntity.class);
		verify(departmentRepositoryMock).findByNameAndFolderNameAndBatchEntityId(DEPARTMENT_NAME, "", BATCH_ID);
		verify(departmentCreatorMock).save(captor.capture());
		assertThat(captor.getValue().getFolderName()).isEqualTo("");
	}

	@Test
	void getOrCreateDepartment_shouldRefetchWhenCreatedConcurrently() {
		final var batchEntity = BatchEntity.builder()
			.withId(BATCH_ID)
			.build();
		final var expectedEntity = DepartmentEntity.builder()
			.withName(DEPARTMENT_NAME)
			.withBatchEntity(batchEntity)
			.build();

		// First lookup finds nothing, the concurrent insert loses the race, the second lookup finds the committed row.
		when(departmentRepositoryMock.findByNameAndFolderNameAndBatchEntityId(DEPARTMENT_NAME, FOLDER_NAME, BATCH_ID))
			.thenReturn(Optional.empty(), Optional.of(expectedEntity));
		when(departmentCreatorMock.save(any(DepartmentEntity.class)))
			.thenThrow(new DataIntegrityViolationException("duplicate"));

		final var result = departmentService.getOrCreateDepartment(DEPARTMENT_NAME, FOLDER_NAME, batchEntity);
		assertThat(result).isEqualTo(expectedEntity);

		verify(departmentRepositoryMock, times(2)).findByNameAndFolderNameAndBatchEntityId(DEPARTMENT_NAME, FOLDER_NAME, BATCH_ID);
		verify(departmentCreatorMock).save(any(DepartmentEntity.class));
	}

	@Test
	void getOrCreateDepartment_shouldThrowWhenConcurrentInsertAndStillNotFound() {
		final var batchEntity = BatchEntity.builder()
			.withId(BATCH_ID)
			.build();

		when(departmentRepositoryMock.findByNameAndFolderNameAndBatchEntityId(DEPARTMENT_NAME, FOLDER_NAME, BATCH_ID))
			.thenReturn(Optional.empty());
		when(departmentCreatorMock.save(any(DepartmentEntity.class)))
			.thenThrow(new DataIntegrityViolationException("duplicate"));

		assertThatThrownBy(() -> departmentService.getOrCreateDepartment(DEPARTMENT_NAME, FOLDER_NAME, batchEntity))
			.isInstanceOf(Problem.class)
			.hasMessageContaining("Department could not be created or found after unique constraint violation");

		verify(departmentRepositoryMock, times(2)).findByNameAndFolderNameAndBatchEntityId(DEPARTMENT_NAME, FOLDER_NAME, BATCH_ID);
		verify(departmentCreatorMock).save(any(DepartmentEntity.class));
	}
}
