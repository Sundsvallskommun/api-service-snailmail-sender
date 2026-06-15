package se.sundsvall.snailmail.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;
import se.sundsvall.snailmail.integration.db.BatchRepository;
import se.sundsvall.snailmail.integration.db.model.BatchEntity;

import static jakarta.transaction.Transactional.TxType.REQUIRES_NEW;

@Component
public class BatchCreator {

	private final BatchRepository batchRepository;

	BatchCreator(final BatchRepository batchRepository) {
		this.batchRepository = batchRepository;
	}

	/**
	 * Inserts the batch in its own new transaction. BatchEntity reports itself as new (see its {@code isNew()}), so
	 * despite its assigned id Spring Data issues a persist() (INSERT) rather than a merge — a merge could silently turn
	 * a concurrent collision into an UPDATE that overwrites the existing row. {@code saveAndFlush} forces the INSERT to
	 * flush synchronously, so a lost race surfaces here as a {@code DataIntegrityViolationException} (primary-key
	 * violation) that rolls back only this inner transaction, leaving the caller's transaction/session clean for a
	 * re-fetch.
	 *
	 * <p>
	 * Must be {@code public}: Spring's proxy-based {@code @Transactional} is silently ignored on non-public methods,
	 * which would collapse this insert into the caller's transaction and defeat the REQUIRES_NEW isolation.
	 */
	@Transactional(REQUIRES_NEW)
	public BatchEntity save(final BatchEntity batchEntity) {
		return batchRepository.saveAndFlush(batchEntity);
	}
}
