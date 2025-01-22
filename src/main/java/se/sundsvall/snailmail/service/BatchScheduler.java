package se.sundsvall.snailmail.service;

import java.time.Duration;
import java.time.OffsetDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.sundsvall.dept44.requestid.RequestId;
import se.sundsvall.dept44.scheduling.Dept44Scheduled;
import se.sundsvall.snailmail.config.BatchProperties;

@Component
public class BatchScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger(BatchScheduler.class);

	private final BatchProperties properties;
	private final SnailMailService snailMailService;

	public BatchScheduler(final BatchProperties properties, final SnailMailService snailMailService) {
		this.snailMailService = snailMailService;
		this.properties = properties;
	}

	/**
	 * Check for batches that has not been triggered, for whatever reason. It is outdated if the batch was created before
	 * the configured duration.
	 */
	@Dept44Scheduled(cron = "${batch.unhandled.cron}",
		name = "${batch.unhandled.name}",
		lockAtMostFor = "${batch.unhandled.lock-at-most-for}",
		maximumExecutionTime = "${batch.unhandled.maximum-execution-time}")
	public void sendUnhandledBatches() {
		RequestId.init();
		LOGGER.info("Checking for unhandled batches");

		try {
			final var outdatedBefore = OffsetDateTime.now().minus(Duration.parse(properties.getOutdatedAfter()));
			final var outdatedBatches = snailMailService.getUnhandledBatches(outdatedBefore);

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
