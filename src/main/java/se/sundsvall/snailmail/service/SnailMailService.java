package se.sundsvall.snailmail.service;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;

import java.util.Optional;

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

	public void sendSnailMail(final String municipalityId, final SendSnailMailRequest request) {

		CitizenExtended citizen = null;
		if (!EnvelopeType.WINDOWED.equals(request.getAttachments().getFirst().getEnvelopeType())) {
			LOGGER.info("Finding information for citizen: {} ", request.getPartyId());
			citizen = citizenIntegration.getCitizen(request.getPartyId());
			validateCitizenAddress(citizen);
		}

		final var batch = batchRepository.findByMunicipalityIdAndId(municipalityId, request.getBatchId())
			.orElseGet(() -> batchRepository.save(BatchEntity.builder().withId(request.getBatchId()).withMunicipalityId(municipalityId).build()));

		final var department = departmentRepository.findByNameAndBatchEntity(request.getDepartment(), batch)
			.orElseGet(() -> departmentRepository.save(Mapper.toDepartment(request.getDepartment(), batch)));

		LOGGER.info("Saving request");
		requestRepository.save(Mapper.toRequest(request, citizen, department));
	}

	private void validateCitizenAddress(CitizenExtended citizen) {
		//Verify that we have name, lastname, address, postal code and city, otherwise we cannot send it as a snail mail.
		Optional.ofNullable(Mapper.toRecipient(citizen))
				.filter(recipientEntity -> isNotBlank(recipientEntity.getGivenName()))
				.filter(recipientEntity -> isNotBlank(recipientEntity.getLastName()))
				.filter(recipientEntity -> isNotBlank(recipientEntity.getAddress()))
				.filter(recipientEntity -> isNotBlank(recipientEntity.getPostalCode()))
				.filter(recipientEntity -> isNotBlank(recipientEntity.getCity()))
			.orElseThrow(() -> Problem.builder()
				.withTitle("Incomplete recipient address information")
				.withStatus(INTERNAL_SERVER_ERROR)
				.withDetail("Missing required fields in the recipient address")
				.build());
	}

	public void sendBatch(final String municipalityId, final String batchId) {

		final var batch = batchRepository.findByMunicipalityIdAndId(municipalityId, batchId)
			.orElseThrow(() -> Problem.builder()
				.withTitle("No batch found")
				.withStatus(INTERNAL_SERVER_ERROR)
				.withDetail("Failed to fetch batch data from database")
				.build());

		sambaIntegration.writeBatchDataToSambaShare(batch);

		batchRepository.delete(batch);
	}

}
