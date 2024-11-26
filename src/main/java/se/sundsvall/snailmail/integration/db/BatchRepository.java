package se.sundsvall.snailmail.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.snailmail.integration.db.model.BatchEntity;

@CircuitBreaker(name = "batchRepository")
public interface BatchRepository extends JpaRepository<BatchEntity, String> {

	Optional<BatchEntity> findByMunicipalityIdAndId(String municipalityId, String id);

	List<BatchEntity> findBatchEntityByCreatedIsBefore(OffsetDateTime createdBefore);

}
