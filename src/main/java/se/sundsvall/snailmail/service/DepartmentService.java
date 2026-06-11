package se.sundsvall.snailmail.service;

import jakarta.transaction.Transactional;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.util.LogUtils;
import se.sundsvall.snailmail.integration.db.DepartmentRepository;
import se.sundsvall.snailmail.integration.db.model.BatchEntity;
import se.sundsvall.snailmail.integration.db.model.DepartmentEntity;

import static jakarta.transaction.Transactional.TxType.REQUIRES_NEW;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static se.sundsvall.snailmail.service.Mapper.toDepartment;

@Service
public class DepartmentService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DepartmentService.class);

	private final DepartmentRepository departmentRepository;
	private final DepartmentCreator departmentCreator;

	public DepartmentService(final DepartmentRepository departmentRepository, final DepartmentCreator departmentCreator) {
		this.departmentRepository = departmentRepository;
		this.departmentCreator = departmentCreator;
	}

	/**
	 * Ensures that a new transaction is started
	 */
	@Transactional(REQUIRES_NEW)
	public DepartmentEntity getOrCreateDepartment(final String departmentName, final String folderName, final BatchEntity batchEntity) {
		// folder_name is part of a department's identity (it selects the output sub-folder) and of the
		// uq_department_batch_name_folder unique constraint. A unique index treats NULLs as distinct, which would
		// defeat the constraint for folder-less departments, so normalize an absent/blank folder to "" and use that
		// for both the lookup and the insert (the integrations already treat null and blank folders identically).
		final var normalizedFolderName = Optional.ofNullable(folderName).filter(folder -> !folder.isBlank()).orElse("");

		final var existingDepartmentEntity = departmentRepository.findByNameAndFolderNameAndBatchEntityId(departmentName, normalizedFolderName, batchEntity.getId());
		if (existingDepartmentEntity.isPresent()) {
			final var entity = existingDepartmentEntity.get();
			LOGGER.info("Found existing department: {}", entity.getId());
			return entity;
		}

		LOGGER.info("Creating new department: {} for batch: {}",
			LogUtils.sanitizeForLogging(departmentName),
			LogUtils.sanitizeForLogging(batchEntity.getId()));

		try {
			// Insert in its own (REQUIRES_NEW) transaction so a unique-constraint violation rolls back only the inner
			// transaction, leaving this transaction/session usable for the re-fetch below.
			return departmentCreator.save(toDepartment(departmentName, normalizedFolderName, batchEntity));
		} catch (final DataIntegrityViolationException e) {
			// Lost the race: another thread/pod already inserted the same department. Re-fetch the committed row.
			// Relies on READ_COMMITTED isolation (see application.yml) so this query sees the row the winning
			// transaction just committed; under REPEATABLE READ the first lookup's snapshot would still hide it.
			LOGGER.info("Department already created concurrently, re-fetching: {}", LogUtils.sanitizeForLogging(departmentName));
			return departmentRepository.findByNameAndFolderNameAndBatchEntityId(departmentName, normalizedFolderName, batchEntity.getId())
				.orElseThrow(() -> Problem.valueOf(INTERNAL_SERVER_ERROR, "Department could not be created or found after unique constraint violation"));
		}
	}
}
