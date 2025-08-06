package se.sundsvall.snailmail.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import se.sundsvall.snailmail.api.model.SendSnailMailRequest;
import se.sundsvall.snailmail.integration.db.BatchRepository;
import se.sundsvall.snailmail.integration.db.model.BatchEntity;

@ExtendWith(MockitoExtension.class)
class BatchServiceTest {

	private static final String BATCH_ID = "123";
	private static final String MUNICIPALITY_ID = "2281";

	@Mock
	private BatchRepository batchRepositoryMock;

	@InjectMocks
	private BatchService batchService;

	@Test
	void getOrCreateBatch_shouldReturnExisting() {
		final var request = SendSnailMailRequest.builder()
			.withBatchId(BATCH_ID)
			.withMunicipalityId(MUNICIPALITY_ID)
			.build();
		final var expectedEntity = BatchEntity.builder()
			.withId(BATCH_ID)
			.withMunicipalityId(MUNICIPALITY_ID)
			.build();

		when(batchRepositoryMock.findByMunicipalityIdAndId(MUNICIPALITY_ID, BATCH_ID)).thenReturn(Optional.of(expectedEntity));

		final var result = batchService.getOrCreateBatch(request);
		assertThat(BATCH_ID).isEqualTo(result.getId());
		assertThat(MUNICIPALITY_ID).isEqualTo(result.getMunicipalityId());

		verify(batchRepositoryMock, never()).save(any());
	}

	@Test
	void getOrCreateBatch_shouldCreateNewIfNotFound() {
		final var request = SendSnailMailRequest.builder()
			.withBatchId(BATCH_ID)
			.withMunicipalityId(MUNICIPALITY_ID)
			.build();
		final var expectedEntity = BatchEntity.builder()
			.withId(BATCH_ID)
			.withMunicipalityId(MUNICIPALITY_ID)
			.build();

		when(batchRepositoryMock.save(any(BatchEntity.class))).thenReturn(expectedEntity);

		final var result = batchService.getOrCreateBatch(request);
		assertThat(BATCH_ID).isEqualTo(result.getId());
		assertThat(MUNICIPALITY_ID).isEqualTo(result.getMunicipalityId());

		final var captor = ArgumentCaptor.forClass(BatchEntity.class);
		verify(batchRepositoryMock).save(captor.capture());

		final var value = captor.getValue();
		assertThat(BATCH_ID).isEqualTo(value.getId());
		assertThat(MUNICIPALITY_ID).isEqualTo(value.getMunicipalityId());
	}

	@Test
	void deleteBatch() {
		final var batchEntity = BatchEntity.builder()
			.withId(BATCH_ID)
			.build();

		batchService.deleteBatch(batchEntity);

		verify(batchRepositoryMock).delete(eq(batchEntity));
	}

	@Test
	void createBatch() {
		final var request = SendSnailMailRequest.builder()
			.withBatchId(BATCH_ID)
			.withMunicipalityId(MUNICIPALITY_ID)
			.build();

		batchService.createBatch(request);

		final var captor = ArgumentCaptor.forClass(BatchEntity.class);
		verify(batchRepositoryMock).save(captor.capture());

		final var value = captor.getValue();
		assertThat(BATCH_ID).isEqualTo(value.getId());
		assertThat(MUNICIPALITY_ID).isEqualTo(value.getMunicipalityId());
	}
}
