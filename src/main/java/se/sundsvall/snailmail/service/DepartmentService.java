package se.sundsvall.snailmail.service;

import static jakarta.transaction.Transactional.TxType.REQUIRES_NEW;
import static se.sundsvall.snailmail.service.Mapper.toDepartment;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.sundsvall.dept44.util.LogUtils;
import se.sundsvall.snailmail.integration.db.DepartmentRepository;
import se.sundsvall.snailmail.integration.db.model.BatchEntity;
import se.sundsvall.snailmail.integration.db.model.DepartmentEntity;

@Service
public class DepartmentService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DepartmentService.class);

	private final DepartmentRepository departmentRepository;

	public DepartmentService(final DepartmentRepository departmentRepository) {
		this.departmentRepository = departmentRepository;
	}

	/**
	 * Ensures that a new transaction is started
	 */
	@Transactional(REQUIRES_NEW)
	public DepartmentEntity getOrCreateDepartment(final String departmentName, final BatchEntity batchEntity) {
		final var existingDepartmentEntity = departmentRepository.findByNameAndBatchEntityId(departmentName, batchEntity.getId());
		if (existingDepartmentEntity.isPresent()) {
			final var entity = existingDepartmentEntity.get();
			LOGGER.info("Found existing department: {}", entity.getId());
			return entity;
		}

		LOGGER.info("Creating new department: {} for batch: {}",
			LogUtils.sanitizeForLogging(departmentName),
			LogUtils.sanitizeForLogging(batchEntity.getId()));
		final var entity = toDepartment(departmentName, batchEntity);
		return departmentRepository.save(entity);
	}
}
