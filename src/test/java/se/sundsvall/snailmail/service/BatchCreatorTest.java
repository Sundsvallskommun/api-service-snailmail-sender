package se.sundsvall.snailmail.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.snailmail.integration.db.BatchRepository;
import se.sundsvall.snailmail.integration.db.model.BatchEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BatchCreatorTest {

	@Mock
	private BatchRepository batchRepositoryMock;

	@InjectMocks
	private BatchCreator batchCreator;

	@Test
	void save() {
		final var entity = BatchEntity.builder()
			.withId("batchId")
			.build();

		when(batchRepositoryMock.saveAndFlush(entity)).thenReturn(entity);

		final var result = batchCreator.save(entity);

		assertThat(result).isEqualTo(entity);
		verify(batchRepositoryMock).saveAndFlush(entity);
		verifyNoMoreInteractions(batchRepositoryMock);
	}
}
