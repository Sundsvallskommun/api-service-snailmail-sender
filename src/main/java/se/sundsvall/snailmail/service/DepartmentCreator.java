package se.sundsvall.snailmail.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;
import se.sundsvall.snailmail.integration.db.DepartmentRepository;
import se.sundsvall.snailmail.integration.db.model.DepartmentEntity;

import static jakarta.transaction.Transactional.TxType.REQUIRES_NEW;

@Component
public class DepartmentCreator {

	private final DepartmentRepository departmentRepository;

	DepartmentCreator(final DepartmentRepository departmentRepository) {
		this.departmentRepository = departmentRepository;
	}

	/**
	 * Saves the department in its own new transaction. If a concurrent thread/pod has already inserted the same
	 * department, the unique-constraint violation surfaces here synchronously (the IDENTITY id forces an immediate
	 * insert) and rolls back only this transaction, leaving the caller's transaction/session clean for a re-fetch.
	 *
	 * <p>
	 * Must be {@code public}: Spring's proxy-based {@code @Transactional} is silently ignored on non-public
	 * methods, which would collapse this save into the caller's transaction and defeat the REQUIRES_NEW isolation.
	 */
	@Transactional(REQUIRES_NEW)
	public DepartmentEntity save(final DepartmentEntity departmentEntity) {
		return departmentRepository.save(departmentEntity);
	}
}
