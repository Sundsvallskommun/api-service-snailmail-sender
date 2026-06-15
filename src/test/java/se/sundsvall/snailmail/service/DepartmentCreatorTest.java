package se.sundsvall.snailmail.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.snailmail.integration.db.DepartmentRepository;
import se.sundsvall.snailmail.integration.db.model.DepartmentEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepartmentCreatorTest {

	@Mock
	private DepartmentRepository departmentRepositoryMock;

	@InjectMocks
	private DepartmentCreator departmentCreator;

	@Test
	void save() {
		final var entity = DepartmentEntity.builder()
			.withName("dept44")
			.build();

		when(departmentRepositoryMock.save(entity)).thenReturn(entity);

		final var result = departmentCreator.save(entity);

		assertThat(result).isEqualTo(entity);
		verify(departmentRepositoryMock).save(entity);
		verifyNoMoreInteractions(departmentRepositoryMock);
	}
}
