package se.sundsvall.snailmail.service;

import java.time.Duration;
import java.time.OffsetDateTime;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import se.sundsvall.dept44.requestid.RequestId;
import se.sundsvall.snailmail.config.BatchProperties;

@Component
public class BatchScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger(BatchScheduler.class);

	private final BatchProperties properties;
	private final SnailMailService snailMailService;

	public BatchScheduler(BatchProperties properties, SnailMailService snailMailService) {
		this.snailMailService = snailMailService;
		this.properties = properties;
	}

	/**
	 * Check for batches that has not been triggered, for whatever reason.
	 * It is outdated if the batch was created before the configured duration.
	 */
	@Scheduled(fixedRateString = "#{@batchProperties.getCheckInterval()}", initialDelayString = "#{@batchProperties.getInitialDelay()}")
	@SchedulerLock(name = "#{@batchProperties.getName()}", lockAtMostFor = "#{@batchProperties.getLockAtMostFor()}")
	public void sendUnhandledBatches() {
		RequestId.init();
		LOGGER.info("Checking for unhandled batches");

		try {
			var outdatedBefore = OffsetDateTime.now().minus(Duration.parse(properties.getOutdatedAfter()));
			var outdatedBatches = snailMailService.getUnhandledBatches(outdatedBefore);

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
