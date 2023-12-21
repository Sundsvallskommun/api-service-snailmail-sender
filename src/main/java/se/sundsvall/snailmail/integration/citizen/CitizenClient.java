package se.sundsvall.snailmail.integration.citizen;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

import java.util.List;
import java.util.Optional;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import generated.se.sundsvall.citizen.CitizenExtended;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@FeignClient(
	name = CitizenIntegration.INTEGRATION_NAME,
	url = "${integration.citizen.base-url}",
	configuration = CitizenIntegrationConfiguration.class)
@CircuitBreaker(name = CitizenIntegration.INTEGRATION_NAME)
interface CitizenClient {

	/**
	 * Fetch a batch of citizens by personIds
	 * Not used for now.
	 * @param showClassified true to fetch classified citizens
	 * @param personIds list of personIds
	 * @return a list of {@link CitizenExtended}
	 */
	@PostMapping(path ="/citizen/batch", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	List<CitizenExtended> getCitizens(@RequestParam(name = "ShowClassified") final boolean showClassified, @RequestBody final List<String> personIds);

	/**
	 * Fetch a citizen by personId
	 * @param personId the personId
	 * @return a {@link CitizenExtended}
	 */
	@GetMapping(path ="/citizen/{personId}", produces = TEXT_PLAIN_VALUE, consumes = APPLICATION_JSON_VALUE)
	Optional<CitizenExtended> getCitizen(@PathVariable(name = "personId") String personId);

}
