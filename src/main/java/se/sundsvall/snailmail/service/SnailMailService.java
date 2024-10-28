package se.sundsvall.snailmail.service;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
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
import se.sundsvall.snailmail.integration.db.model.DepartmentEntity;
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

	public static final String GIVEN_NAME = "Given Name";
	public static final String LAST_NAME = "Last Name";
	public static final String ADDRESS = "Address";
	public static final String POSTAL_CODE = "Postal Code";
	public static final String CITY = "City";

	public SnailMailService(final SambaIntegration sambaIntegration, final BatchRepository batchRepository, final DepartmentRepository departmentRepository, final RequestRepository requestRepository, final CitizenIntegration citizenIntegration) {
		this.batchRepository = batchRepository;
		this.departmentRepository = departmentRepository;
		this.requestRepository = requestRepository;
		this.sambaIntegration = sambaIntegration;
		this.citizenIntegration = citizenIntegration;
	}

	public void sendSnailMail(final String municipalityId, final SendSnailMailRequest request, final String issuer) {

		CitizenExtended citizen = null;
		if (!EnvelopeType.WINDOWED.equals(request.getAttachments().getFirst().getEnvelopeType())) {
			LOGGER.info("Finding information for citizen: {} ", request.getPartyId());
			citizen = citizenIntegration.getCitizen(request.getPartyId());
			validateCitizenAddress(citizen);
		}

		LOGGER.info("Saving request for batch: {} and department: {} ", request.getBatchId(), request.getDepartment());

		final var batch = getBatchEntity(municipalityId, request, issuer);
		final var department = getDepartmentEntity(request, batch);

		requestRepository.save(Mapper.toRequest(request, citizen, department));
	}

	private @NotNull BatchEntity getBatchEntity(String municipalityId, SendSnailMailRequest request, String issuer) {
		LOGGER.info("Finding batch: {} and/or saving", request.getBatchId());
		return batchRepository.findByMunicipalityIdAndId(municipalityId, request.getBatchId())
			.orElseGet(() -> batchRepository.save(BatchEntity.builder().withId(request.getBatchId()).withIssuer(issuer).withMunicipalityId(municipalityId).build()));
	}

	private @NotNull DepartmentEntity getDepartmentEntity(SendSnailMailRequest request, BatchEntity batch) {
		LOGGER.info("Finding department: {} and/or saving", request.getDepartment());
		return departmentRepository.findByNameAndBatchEntityId(request.getDepartment(), batch.getId())
			.orElseGet(() -> departmentRepository.save(Mapper.toDepartment(request.getDepartment(), batch)));
	}

	/**
	 * Validate that the citizen has all required address information
	 * If any required field is missing, a Problem will be thrown describing the missing fields
	 */
	private void validateCitizenAddress(CitizenExtended citizen) {
		var recipientEntity = Optional.ofNullable(Mapper.toRecipient(citizen))
			.orElseThrow(() -> Problem.builder()
				.withTitle("No address information found for citizen")
				.withStatus(INTERNAL_SERVER_ERROR)
				.withDetail("No address information found for citizen with partyId: " + citizen.getPersonId())
				.build());

		//Check each required field in the address and save it in the map
		Map<String, Boolean> validatedFields = new LinkedHashMap<>();   //Preserve insertion order
		validatedFields.put(GIVEN_NAME, isNotBlank(recipientEntity.getGivenName()));
		validatedFields.put(LAST_NAME, isNotBlank(recipientEntity.getLastName()));
		validatedFields.put(ADDRESS, isNotBlank(recipientEntity.getAddress()));
		validatedFields.put(POSTAL_CODE, isNotBlank(recipientEntity.getPostalCode()));
		validatedFields.put(CITY, isNotBlank(recipientEntity.getCity()));

		//If there are any missing fields (i.e. false in any of the values of the map), throw a Problem
		if (validatedFields.values().stream().anyMatch(Boolean.FALSE::equals)) {
			//Create a string with the missing fields
			var missingFields = validatedFields.entrySet().stream()
				.filter(entry -> !entry.getValue())
				.map(Map.Entry::getKey)
				.collect(Collectors.joining(", ", "Missing fields: ", ""));

			throw Problem.builder()
				.withTitle("Citizen with partyId: " + citizen.getPersonId() + " is missing required address information")
				.withStatus(INTERNAL_SERVER_ERROR)
				.withDetail(missingFields)
				.build();
		}
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
