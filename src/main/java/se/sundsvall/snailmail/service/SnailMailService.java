package se.sundsvall.snailmail.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.problem.Problem;
import se.sundsvall.snailmail.api.model.SendSnailMailRequest;
import se.sundsvall.snailmail.integration.db.RequestRepository;
import se.sundsvall.snailmail.integration.db.model.BatchEntity;
import se.sundsvall.snailmail.integration.samba.SambaIntegration;
import se.sundsvall.snailmail.integration.sftp.SftpIntegration;

import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;
import static se.sundsvall.snailmail.service.Mapper.toRecipient;
import static se.sundsvall.snailmail.service.Mapper.toRequest;

@Service
public class SnailMailService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SnailMailService.class);

	private final SambaIntegration sambaIntegration;
	private final SftpIntegration sftpIntegration;

	private final BatchService batchService;
	private final DepartmentService departmentService;
	private final RequestRepository requestRepository;
	private final Semaphore semaphore;

	@Value("${integration.samba.active}")
	private boolean sambaActive;

	@Value("${integration.sftp.active}")
	private boolean sftpActive;

	public SnailMailService(final SambaIntegration sambaIntegration,
		final SftpIntegration sftpIntegration,
		final BatchService batchService,
		final DepartmentService departmentService,
		final RequestRepository requestRepository, Semaphore semaphore) {
		this.sftpIntegration = sftpIntegration;
		this.batchService = batchService;
		this.departmentService = departmentService;
		this.requestRepository = requestRepository;
		this.sambaIntegration = sambaIntegration;
		this.semaphore = semaphore;
	}

	/**
	 * Sends a snail mail. Since this method may experience high concurrency with lots of reading and saving to the DB it is
	 * prone to race conditions. To mitigate this, a semaphore is used to limit the number of concurrent requests to 1.
	 *
	 * @param request the request containing the snail mail details
	 */
	@Transactional
	public void sendSnailMail(final SendSnailMailRequest request) {
		try {
			// Quick-fix (hopefully) to avoid race conditions.
			if (!semaphore.tryAcquire(10, TimeUnit.SECONDS)) {
				throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Couldn't acquire lock for sending snail mail request");
			}

			var recipientEntity = toRecipient(request.getAddress());
			var batch = batchService.getOrCreateBatch(request);
			var departmentEntity = departmentService.getOrCreateDepartment(request.getDepartment(), request.getFolderName(), batch);

			requestRepository.save(toRequest(request, recipientEntity, departmentEntity));
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} finally {
			semaphore.release();
		}
	}

	@Transactional
	public void sendBatch(final String municipalityId, final String batchId) {
		var batch = batchService.findBatchByMunicipalityIdAndId(municipalityId, batchId)
			.orElseThrow(() -> Problem.builder()
				.withTitle("No batch found")
				.withStatus(INTERNAL_SERVER_ERROR)
				.withDetail("Failed to fetch batch data from database")
				.build());

		if (sambaActive) {
			LOGGER.info("Writing batch data to Samba share");
			sambaIntegration.writeBatchDataToSambaShare(batch);
		}
		if (sftpActive) {
			LOGGER.info("Writing batch data to SFTP server");
			sftpIntegration.writeBatchDataToSftp(batch);
		}
		if (!sambaActive && !sftpActive) {
			LOGGER.warn("No integration active, nothing to do");
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "No integration active");
		}

		batchService.deleteBatch(batch);
	}

	/**
	 * Get all batches that have not been handled for a certain duration
	 *
	 * @param  outdatedBefore the time before which the batch is considered outdated
	 * @return                a list of unhandled batches
	 */
	@Transactional
	public List<BatchEntity> getUnhandledBatches(OffsetDateTime outdatedBefore) {
		return batchService.findOutdatedBatches(outdatedBefore);
	}
}
