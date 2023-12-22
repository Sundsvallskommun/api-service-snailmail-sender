package se.sundsvall.snailmail.integration.citizen;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

import java.util.Optional;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import generated.se.sundsvall.citizen.CitizenExtended;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@FeignClient(
	name = CitizenIntegration.INTEGRATION_NAME,
	url = "${integration.citizen.base-url}",
	configuration = CitizenIntegrationConfiguration.class)
@CircuitBreaker(name = CitizenIntegration.INTEGRATION_NAME)
interface CitizenClient {

	/**
	 * Fetch a citizen by personId, never asks for classified data
	 * @param personId the personId
	 * @return a {@link CitizenExtended}
	 */
	@GetMapping(path ="/citizen/{personId}?ShowClassified=false", produces = TEXT_PLAIN_VALUE, consumes = APPLICATION_JSON_VALUE)
	Optional<CitizenExtended> getCitizen(@PathVariable(name = "personId") String personId);
}
