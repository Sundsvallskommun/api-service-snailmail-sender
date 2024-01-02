package se.sundsvall.snailmail.integration.db;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import se.sundsvall.snailmail.integration.db.model.Department;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@CircuitBreaker(name = "departmentRepository")
public interface DepartmentRepository extends JpaRepository<Department, Long> {

	Optional<Department> findByName(String departmentName);

}
