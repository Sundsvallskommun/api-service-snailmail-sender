package se.sundsvall.snailmail.integration.citizen;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static se.sundsvall.snailmail.integration.citizen.configuration.CitizenIntegrationConfiguration.CLIENT_ID;

import generated.se.sundsvall.citizen.CitizenExtended;
import java.util.Optional;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import se.sundsvall.snailmail.integration.citizen.configuration.CitizenIntegrationConfiguration;

@FeignClient(
	name = CLIENT_ID,
	url = "${integration.citizen.base-url}",
	configuration = CitizenIntegrationConfiguration.class)
public interface CitizenClient {

	/**
	 * Fetch a citizen by personId, never asks for classified data
	 *
	 * @param  personId the personId
	 * @return          a {@link CitizenExtended}
	 */
	@GetMapping(path = "/{personId}?ShowClassified=false", produces = TEXT_PLAIN_VALUE, consumes = APPLICATION_JSON_VALUE)
	Optional<CitizenExtended> getCitizen(@PathVariable(name = "personId") String personId);

}
