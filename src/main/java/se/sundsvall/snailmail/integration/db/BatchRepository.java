package se.sundsvall.snailmail.integration.db;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import se.sundsvall.snailmail.integration.db.model.BatchEntity;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@CircuitBreaker(name = "batchRepository")
public interface BatchRepository extends JpaRepository<BatchEntity, String> {

	Optional<BatchEntity> findByMunicipalityIdAndId(String municipalityId, String id);

}
