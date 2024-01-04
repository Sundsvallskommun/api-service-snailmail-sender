package se.sundsvall.snailmail.integration.db;

import org.springframework.data.jpa.repository.JpaRepository;

import se.sundsvall.snailmail.integration.db.model.RequestEntity;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@CircuitBreaker(name = "requestRepository")
public interface RequestRepository extends JpaRepository<RequestEntity, Long> {

}
