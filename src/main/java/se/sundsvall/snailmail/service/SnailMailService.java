package se.sundsvall.snailmail.service;

import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;
import static se.sundsvall.snailmail.service.Mapper.toBatchEntity;
import static se.sundsvall.snailmail.service.Mapper.toDepartment;
import static se.sundsvall.snailmail.service.Mapper.toRecipient;
import static se.sundsvall.snailmail.service.Mapper.toRequest;

import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import se.sundsvall.snailmail.api.model.SendSnailMailRequest;
import se.sundsvall.snailmail.integration.db.BatchRepository;
import se.sundsvall.snailmail.integration.db.DepartmentRepository;
import se.sundsvall.snailmail.integration.db.RequestRepository;
import se.sundsvall.snailmail.integration.db.model.BatchEntity;
import se.sundsvall.snailmail.integration.db.model.DepartmentEntity;
import se.sundsvall.snailmail.integration.samba.SambaIntegration;
import se.sundsvall.snailmail.integration.sftp.SftpIntegration;

@Service
@Transactional
public class SnailMailService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SnailMailService.class);

	private final SambaIntegration sambaIntegration;
	private final SftpIntegration sftpIntegration;

	private final BatchRepository batchRepository;
	private final DepartmentRepository departmentRepository;
	private final RequestRepository requestRepository;

	@Value("${integration.samba.active}")
	private boolean sambaActive;

	@Value("${integration.sftp.active}")
	private boolean sftpActive;

	public SnailMailService(final SambaIntegration sambaIntegration,
		final SftpIntegration sftpIntegration,
		final BatchRepository batchRepository,
		final DepartmentRepository departmentRepository,
		final RequestRepository requestRepository) {
		this.sftpIntegration = sftpIntegration;
		this.batchRepository = batchRepository;
		this.departmentRepository = departmentRepository;
		this.requestRepository = requestRepository;
		this.sambaIntegration = sambaIntegration;
	}

	public synchronized void sendSnailMail(final SendSnailMailRequest request) {
		// Create recipient entity based on the address or citizen information
		var recipient = toRecipient(request.getAddress());
		LOGGER.info("Saving request for batch: {} and department: {} ", request.getBatchId(), request.getDepartment());
		var batch = getBatchEntity(request);
		var department = getDepartmentEntity(request, batch);
		requestRepository.save(toRequest(request, recipient, department));
	}

	private @NotNull BatchEntity getBatchEntity(SendSnailMailRequest request) {
		LOGGER.info("Getting batch: {} or saving a new one", request.getBatchId());
		return batchRepository.findByMunicipalityIdAndId(request.getMunicipalityId(), request.getBatchId())
			.orElseGet(() -> batchRepository.save(toBatchEntity(request)));
	}

	private @NotNull DepartmentEntity getDepartmentEntity(SendSnailMailRequest request, BatchEntity batch) {
		LOGGER.info("Getting department: {} or saving a new one", request.getDepartment());
		return departmentRepository.findByNameAndBatchEntityId(request.getDepartment(), batch.getId())
			.orElseGet(() -> departmentRepository.save(toDepartment(request.getDepartment(), batch)));
	}

	public void sendBatch(final String municipalityId, final String batchId) {
		var batch = batchRepository.findByMunicipalityIdAndId(municipalityId, batchId)
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

		batchRepository.delete(batch);
	}

	/**
	 * Get all batches that have not been handled for a certain duration
	 *
	 * @param  outdatedBefore the time before which the batch is considered outdated
	 * @return                a list of unhandled batches
	 */
	public List<BatchEntity> getUnhandledBatches(OffsetDateTime outdatedBefore) {
		return batchRepository.findBatchEntityByCreatedIsBefore(outdatedBefore);
	}
}
