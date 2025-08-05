package se.sundsvall.snailmail.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

	@Mock
	private BatchRepository batchRepositoryMock;

	@InjectMocks
	private BatchService batchService;

	@Test
	void createEntity() {
		final var batchId = "123";
		final var municipalityId = "2281";
		final var request = SendSnailMailRequest.builder()
			.withBatchId(batchId)
			.withMunicipalityId(municipalityId)
			.build();
		final var expectedEntity = BatchEntity.builder().withId(batchId).build();

		when(batchRepositoryMock.save(any(BatchEntity.class))).thenReturn(expectedEntity);

		final var result = batchService.createEntity(request);

		assertEquals(batchId, result.getId());

		final var captor = ArgumentCaptor.forClass(BatchEntity.class);
		verify(batchRepositoryMock).save(captor.capture());

		final var value = captor.getValue();
		assertEquals(batchId, value.getId());
		assertEquals(municipalityId, value.getMunicipalityId());
	}
}
