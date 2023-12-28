package se.sundsvall.snailmail.service;

import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;

import se.sundsvall.snailmail.api.model.SendSnailMailRequest;
import se.sundsvall.snailmail.integration.citizen.CitizenIntegration;
import se.sundsvall.snailmail.integration.db.BatchRepository;
import se.sundsvall.snailmail.integration.db.DepartmentRepository;
import se.sundsvall.snailmail.integration.db.RequestRepository;
import se.sundsvall.snailmail.integration.db.model.Batch;
import se.sundsvall.snailmail.integration.samba.SambaIntegration;

@Service
public class SnailMailService {

	private final BatchRepository batchRepository;

	private final DepartmentRepository departmentRepository;

	private final SambaIntegration sambaIntegration;

	private final CitizenIntegration citizenIntegration;

	private final RequestRepository requestRepository;

	public SnailMailService(final SambaIntegration sambaIntegration, final BatchRepository batchRepository, final DepartmentRepository departmentRepository, final RequestRepository requestRepository, final CitizenIntegration citizenIntegration) {
		this.sambaIntegration = sambaIntegration;
		this.batchRepository = batchRepository;
		this.departmentRepository = departmentRepository;
		this.requestRepository = requestRepository;
		this.citizenIntegration = citizenIntegration;
	}


	public void saveSnailMailForBatch(final SendSnailMailRequest request) {

		try {
			sambaIntegration.writeBatchDataToSambaShare(request);
		} catch (final IOException e) {
			throw Problem.builder()
				.withTitle("Failed when writing files to samba share")
				.withStatus(INTERNAL_SERVER_ERROR)
				.withDetail(e.getMessage())
				.build();
		}
	}

	public void sendSnailMail(final SendSnailMailRequest request) {

		final var batch = batchRepository.findById(request.getBatchId())
			.orElseGet(() -> batchRepository.save(Batch.builder().withId(request.getBatchId()).build()));

		final var department = departmentRepository.findByName(request.getDepartment())
			.orElseGet(() -> departmentRepository
				.save(Mapper.toDepartment(request.getDepartment(), batch)));

		final var citizen = citizenIntegration.getCitizen(request.getPartyId());
		requestRepository.save(Mapper.toRequest(request, citizen, department));
	}

}
