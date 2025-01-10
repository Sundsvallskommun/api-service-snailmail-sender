package se.sundsvall.snailmail.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.snailmail.integration.db.model.RequestEntity;

@CircuitBreaker(name = "requestRepository")
public interface RequestRepository extends JpaRepository<RequestEntity, Long> {

}
