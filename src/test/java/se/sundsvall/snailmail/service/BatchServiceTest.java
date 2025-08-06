package se.sundsvall.snailmail.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
		assertEquals(BATCH_ID, result.getId());
		assertEquals(MUNICIPALITY_ID, result.getMunicipalityId());

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
		assertEquals(BATCH_ID, result.getId());
		assertEquals(MUNICIPALITY_ID, result.getMunicipalityId());

		final var captor = ArgumentCaptor.forClass(BatchEntity.class);
		verify(batchRepositoryMock).save(captor.capture());

		final var value = captor.getValue();
		assertEquals(BATCH_ID, value.getId());
		assertEquals(MUNICIPALITY_ID, value.getMunicipalityId());
	}
}
