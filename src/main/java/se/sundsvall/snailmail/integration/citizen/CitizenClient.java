package se.sundsvall.snailmail.integration.citizen;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
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

	@PostMapping(path = "/batch", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	List<CitizenExtended> getCitizens(@RequestParam(name = "ShowClassified") final boolean showClassified, @RequestBody final List<String> personId);
}
