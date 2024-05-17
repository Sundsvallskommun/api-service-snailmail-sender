package se.sundsvall.snailmail.service;

import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;

import se.sundsvall.snailmail.api.model.EnvelopeType;
import se.sundsvall.snailmail.api.model.SendSnailMailRequest;
import se.sundsvall.snailmail.integration.citizen.CitizenIntegration;
import se.sundsvall.snailmail.integration.db.BatchRepository;
import se.sundsvall.snailmail.integration.db.DepartmentRepository;
import se.sundsvall.snailmail.integration.db.RequestRepository;
import se.sundsvall.snailmail.integration.db.model.BatchEntity;
import se.sundsvall.snailmail.integration.samba.SambaIntegration;

import generated.se.sundsvall.citizen.CitizenExtended;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class SnailMailService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SnailMailService.class);

	private final BatchRepository batchRepository;

	private final DepartmentRepository departmentRepository;

	private final RequestRepository requestRepository;

	private final SambaIntegration sambaIntegration;

	private final CitizenIntegration citizenIntegration;


	public SnailMailService(final SambaIntegration sambaIntegration, final BatchRepository batchRepository, final DepartmentRepository departmentRepository, final RequestRepository requestRepository, final CitizenIntegration citizenIntegration) {
		this.batchRepository = batchRepository;
		this.departmentRepository = departmentRepository;
		this.requestRepository = requestRepository;
		this.sambaIntegration = sambaIntegration;
		this.citizenIntegration = citizenIntegration;
	}

	public void sendSnailMail(final SendSnailMailRequest request) {

		CitizenExtended citizen = null;
		if (!EnvelopeType.WINDOWED.equals(request.getAttachments().getFirst().getEnvelopeType())) {
			LOGGER.info("Finding information for citizen: {} ", request.getPartyId());
			citizen = citizenIntegration.getCitizen(request.getPartyId());
		}

		final var batch = batchRepository.findById(request.getBatchId())
			.orElseGet(() -> batchRepository.save(BatchEntity.builder().withId(request.getBatchId()).build()));

		final var department = departmentRepository.findByNameAndBatchEntity(request.getDepartment(), batch)
			.orElseGet(() -> departmentRepository.save(Mapper.toDepartment(request.getDepartment(), batch)));

		LOGGER.info("Saving request");
		requestRepository.save(Mapper.toRequest(request, citizen, department));
	}

	public void sendBatch(final String batchId) {

		final var batch = batchRepository.findById(batchId)
			.orElseThrow(() -> Problem.builder()
				.withTitle("No batch found")
				.withStatus(INTERNAL_SERVER_ERROR)
				.withDetail("Failed to fetch batch data from database")
				.build());

		sambaIntegration.writeBatchDataToSambaShare(batch);

		batchRepository.delete(batch);
	}
}
