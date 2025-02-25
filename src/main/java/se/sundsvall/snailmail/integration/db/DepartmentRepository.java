package se.sundsvall.snailmail.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.snailmail.integration.db.model.DepartmentEntity;

@CircuitBreaker(name = "departmentRepository")
public interface DepartmentRepository extends JpaRepository<DepartmentEntity, Long> {

	Optional<DepartmentEntity> findByNameAndBatchEntityId(String departmentName, String batchEntityId);

}
