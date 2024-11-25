package se.sundsvall.snailmail.service;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;
import static se.sundsvall.snailmail.service.Mapper.toBatchEntity;
import static se.sundsvall.snailmail.service.Mapper.toDepartment;
import static se.sundsvall.snailmail.service.Mapper.toRecipient;
import static se.sundsvall.snailmail.service.Mapper.toRequest;

import generated.se.sundsvall.citizen.CitizenExtended;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
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
import se.sundsvall.snailmail.integration.db.model.RecipientEntity;
import se.sundsvall.snailmail.integration.samba.SambaIntegration;

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

	public SnailMailService(final SambaIntegration sambaIntegration,
		final BatchRepository batchRepository,
		final DepartmentRepository departmentRepository,
		final RequestRepository requestRepository,
		final CitizenIntegration citizenIntegration) {
		this.batchRepository = batchRepository;
		this.departmentRepository = departmentRepository;
		this.requestRepository = requestRepository;
		this.sambaIntegration = sambaIntegration;
		this.citizenIntegration = citizenIntegration;
	}

	public synchronized void sendSnailMail(final SendSnailMailRequest request) {
		// Create recipient entity based on the address or citizen information
		var recipient = createRecipient(request);
		LOGGER.info("Saving request for batch: {} and department: {} ", request.getBatchId(), request.getDepartment());
		var batch = getBatchEntity(request);
		var department = getDepartmentEntity(request, batch);
		requestRepository.save(toRequest(request, recipient, department));
	}

	private RecipientEntity createRecipient(final SendSnailMailRequest request) {
		if (request.getAddress() != null) {
			return toRecipient(request.getAddress());
		}
		if (!EnvelopeType.WINDOWED.equals(request.getAttachments().getFirst().getEnvelopeType())) {
			LOGGER.info("Finding information for citizen: {} ", request.getPartyId());
			var citizen = citizenIntegration.getCitizen(request.getPartyId());
			validateCitizenAddress(citizen);
			return toRecipient(citizen);
		}

		return null;
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

	/**
	 * Validate that the citizen has all required address information
	 * If any required field is missing, a Problem will be thrown describing the missing fields
	 */
	private void validateCitizenAddress(CitizenExtended citizen) {
		var recipientEntity = Optional.ofNullable(toRecipient(citizen))
			.orElseThrow(() -> Problem.builder()
				.withTitle("No address information found for citizen")
				.withStatus(INTERNAL_SERVER_ERROR)
				.withDetail("No address information found for citizen with partyId: " + citizen.getPersonId())
				.build());

		// Check each required field in the address and save it in the map
		Map<String, Boolean> validatedFields = new LinkedHashMap<>();   // Preserve insertion order
		validatedFields.put(GIVEN_NAME, isNotBlank(recipientEntity.getGivenName()));
		validatedFields.put(LAST_NAME, isNotBlank(recipientEntity.getLastName()));
		validatedFields.put(ADDRESS, isNotBlank(recipientEntity.getAddress()));
		validatedFields.put(POSTAL_CODE, isNotBlank(recipientEntity.getPostalCode()));
		validatedFields.put(CITY, isNotBlank(recipientEntity.getCity()));

		// If there are any missing fields (i.e. false in any of the values of the map), throw a Problem
		if (validatedFields.values().stream().anyMatch(Boolean.FALSE::equals)) {
			// Create a string with the missing fields
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
