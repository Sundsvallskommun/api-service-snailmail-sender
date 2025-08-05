package se.sundsvall.snailmail.service;

import static jakarta.transaction.Transactional.TxType.REQUIRES_NEW;
import static se.sundsvall.snailmail.service.Mapper.toBatchEntity;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import se.sundsvall.snailmail.api.model.SendSnailMailRequest;
import se.sundsvall.snailmail.integration.db.BatchRepository;
import se.sundsvall.snailmail.integration.db.model.BatchEntity;

@Service
public class BatchService {

	private final BatchRepository batchRepository;

	public BatchService(final BatchRepository batchRepository) {
		this.batchRepository = batchRepository;
	}

	public BatchRepository getRepository() {
		return batchRepository;
	}

	/**
	 * Ensures that a new transaction is started and committed when inserting batch entities.
	 */
	@Transactional(REQUIRES_NEW)
	public BatchEntity createEntity(SendSnailMailRequest request) {
		final var entity = toBatchEntity(request);
		return batchRepository.save(entity);
	}
}
