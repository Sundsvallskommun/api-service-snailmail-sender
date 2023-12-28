package se.sundsvall.snailmail.integration.db;

import org.springframework.data.jpa.repository.JpaRepository;

import se.sundsvall.snailmail.integration.db.model.Batch;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@CircuitBreaker(name = "batchRepository")
public interface BatchRepository extends JpaRepository<Batch, String> {

}
