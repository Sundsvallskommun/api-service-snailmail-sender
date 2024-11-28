package se.sundsvall.snailmail.service;

import jakarta.transaction.Transactional;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import se.sundsvall.snailmail.api.model.SendSnailMailRequest;
import se.sundsvall.snailmail.integration.db.BatchRepository;
import se.sundsvall.snailmail.integration.db.DepartmentRepository;
import se.sundsvall.snailmail.integration.db.RequestRepository;
import se.sundsvall.snailmail.integration.db.model.BatchEntity;
import se.sundsvall.snailmail.integration.db.model.DepartmentEntity;
import se.sundsvall.snailmail.integration.samba.SambaIntegration;

import java.time.OffsetDateTime;
import java.util.List;

import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;
import static se.sundsvall.snailmail.service.Mapper.toBatchEntity;
import static se.sundsvall.snailmail.service.Mapper.toDepartment;
import static se.sundsvall.snailmail.service.Mapper.toRecipient;
import static se.sundsvall.snailmail.service.Mapper.toRequest;

@Service
@Transactional
public class SnailMailService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SnailMailService.class);

	private final SambaIntegration sambaIntegration;
	private final BatchRepository batchRepository;
	private final DepartmentRepository departmentRepository;
	private final RequestRepository requestRepository;

	public SnailMailService(final SambaIntegration sambaIntegration,
		final BatchRepository batchRepository,
		final DepartmentRepository departmentRepository,
		final RequestRepository requestRepository) {
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

		sambaIntegration.writeBatchDataToSambaShare(batch);

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
