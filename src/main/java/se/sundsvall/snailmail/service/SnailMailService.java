package se.sundsvall.snailmail.service;

import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;
import static se.sundsvall.snailmail.service.Mapper.toBatchEntity;
import static se.sundsvall.snailmail.service.Mapper.toDepartment;
import static se.sundsvall.snailmail.service.Mapper.toRecipient;
import static se.sundsvall.snailmail.service.Mapper.toRequest;

import java.time.OffsetDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

	/**
	 * Sends a snail mail.
	 * Since this method may experience high concurrency with lots of reading and saving to the DB it is prone to race
	 * conditions.
	 * In the rare case we get a DataIntegrityViolationException, we assume another thread has already created the batch (or
	 * department) and we fetch it instead.
	 * If it fails to create and fetch the batch or department, we cannot do anything, hopefully should never happen but it
	 * is handled.
	 * 
	 * @param request the request containing the snail mail details
	 */
	@Transactional
	public void sendSnailMail(final SendSnailMailRequest request) {
		var recipientEntity = toRecipient(request.getAddress());
		var batch = getOrCreateBatchSafely(request);
		var departmentEntity = getOrCreateDepartmentSafely(request.getDepartment(), batch);

		requestRepository.save(toRequest(request, recipientEntity, departmentEntity));
	}

	private BatchEntity getOrCreateBatchSafely(SendSnailMailRequest request) {
		// Try to find existing batch
		var existingBatchEntity = batchRepository.findByMunicipalityIdAndId(request.getMunicipalityId(), request.getBatchId());
		if (existingBatchEntity.isPresent()) {
			return existingBatchEntity.get();
		}

		try {
			// If not found, create a new batch
			LOGGER.info("Creating new batch: {}", request.getBatchId());
			return batchRepository.save(toBatchEntity(request));
		} catch (DataIntegrityViolationException e) {
			// Race condition: another thread might have created the batch, try to fetch it or else throw an error
			LOGGER.info("Batch creation probably failed due to a race condition, fetching existing batch: {}", request.getBatchId());
			return batchRepository.findByMunicipalityIdAndId(request.getMunicipalityId(), request.getBatchId())
				.orElseThrow(() -> Problem.builder()
					.withStatus(INTERNAL_SERVER_ERROR)
					.withTitle("Failed to retrieve or create batch: " + request.getBatchId())
					.build());
		}
	}

	private DepartmentEntity getOrCreateDepartmentSafely(String departmentName, BatchEntity batchEntity) {
		// Try to find existing department
		var existingBatchEntity = departmentRepository.findByNameAndBatchEntityId(departmentName, batchEntity.getId());

		if (existingBatchEntity.isPresent()) {
			return existingBatchEntity.get();
		}

		// If not found, try to create a new department
		try {
			// If not found, create a new batch
			LOGGER.info("Creating new department: {} for batch: {}", departmentName, batchEntity.getId());
			return departmentRepository.save(toDepartment(departmentName, batchEntity));
		} catch (DataIntegrityViolationException e) {
			// Race condition: another thread might have created the department, try and fetch it or else throw an error
			LOGGER.info("Department creation probably failed due to a race condition, fetching existing department: {} for batch: {}", departmentName, batchEntity.getId());
			return departmentRepository.findByNameAndBatchEntityId(batchEntity.getMunicipalityId(), batchEntity.getId())
				.orElseThrow(() -> Problem.builder()
					.withStatus(INTERNAL_SERVER_ERROR)
					.withTitle("Failed to retrieve or create department: " + departmentName + " for batch: " + batchEntity.getId())
					.build());
		}
	}

	@Transactional
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
	@Transactional
	public List<BatchEntity> getUnhandledBatches(OffsetDateTime outdatedBefore) {
		return batchRepository.findBatchEntityByCreatedIsBefore(outdatedBefore);
	}
}
