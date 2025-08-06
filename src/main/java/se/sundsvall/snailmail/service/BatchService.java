package se.sundsvall.snailmail.service;

import static jakarta.transaction.Transactional.TxType.REQUIRES_NEW;
import static se.sundsvall.snailmail.service.Mapper.toBatchEntity;

import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.sundsvall.snailmail.api.model.SendSnailMailRequest;
import se.sundsvall.snailmail.integration.db.BatchRepository;
import se.sundsvall.snailmail.integration.db.model.BatchEntity;

@Service
public class BatchService {

	private static final Logger LOGGER = LoggerFactory.getLogger(BatchService.class);

	private final BatchRepository batchRepository;

	public BatchService(final BatchRepository batchRepository) {
		this.batchRepository = batchRepository;
	}

	public Optional<BatchEntity> findBatchByMunicipalityIdAndId(final String municipalityId, final String id) {
		return batchRepository.findByMunicipalityIdAndId(municipalityId, id);
	}

	public List<BatchEntity> findOutdatedBatches(final OffsetDateTime outdatedBefore) {
		return batchRepository.findBatchEntityByCreatedIsBefore(outdatedBefore);
	}

	public BatchEntity getOrCreateBatch(final SendSnailMailRequest request) {
		final var existingBatchEntity = batchRepository.findByMunicipalityIdAndId(request.getMunicipalityId(), request.getBatchId());
		if (existingBatchEntity.isPresent()) {
			final var entity = existingBatchEntity.get();
			LOGGER.info("Found existing batch: {}", entity.getId());
			return entity;
		}

		LOGGER.info("Creating new batch: {}", request.getBatchId());
		return createBatch(request);
	}

	public void deleteBatch(final BatchEntity batchEntity) {
		batchRepository.delete(batchEntity);
	}

	/**
	 * Ensures that a new transaction is started and committed when inserting entities.
	 */
	@Transactional(REQUIRES_NEW)
	private BatchEntity createBatch(final SendSnailMailRequest request) {
		final var entity = toBatchEntity(request);
		return batchRepository.save(entity);
	}
}
