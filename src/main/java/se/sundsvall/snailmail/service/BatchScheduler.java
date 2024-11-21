package se.sundsvall.snailmail.service;

import java.time.Duration;
import java.time.OffsetDateTime;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import se.sundsvall.dept44.requestid.RequestId;
import se.sundsvall.snailmail.integration.db.BatchRepository;

@Component
public class BatchScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger(BatchScheduler.class);

	private final Duration outdatedAfter;
	private final BatchRepository batchRepository;
	private final SnailMailService snailMailService;

	public BatchScheduler(@Value("${batch.dangling.outdated-after}") final Duration outdatedAfter, BatchRepository batchRepository, SnailMailService snailMailService) {
		this.outdatedAfter = outdatedAfter;
		this.batchRepository = batchRepository;
		this.snailMailService = snailMailService;
	}

	/**
	 * Check for batches that has not been triggered, for whatever reason.
	 * It is outdated if the batch was created before the configured duration.
	 */
	@Scheduled(fixedRateString = "${batch.dangling.check-interval}")
	@SchedulerLock(name = "${batch.dangling.name}", lockAtMostFor = "${batch.dangling.lock-at-most-for}")
	public void sendUnhandledBatches() {
		RequestId.init();
		LOGGER.info("Checking for unhandled batches");

		try {
			var outdatedBefore = OffsetDateTime.now().minus(this.outdatedAfter);
			var outdatedBatches = batchRepository.findBatchEntityByCreatedIsBefore(outdatedBefore);

			if (!outdatedBatches.isEmpty()) {
				LOGGER.info("Found {} unhandled batches, triggering them.", outdatedBatches.size());
				outdatedBatches.forEach(batchEntity -> snailMailService.sendBatch(batchEntity.getMunicipalityId(), batchEntity.getId()));
			} else {
				LOGGER.info("No unhandled batches found");
			}
		} finally {
			RequestId.reset();
		}
	}
}
