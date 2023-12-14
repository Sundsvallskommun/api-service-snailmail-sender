package se.sundsvall.snailmail.integration.citizen;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import generated.se.sundsvall.citizen.Citizen;

@FeignClient(
	name = CitizenIntegration.INTEGRATION_NAME,
	url = "${integration.citizen.base-url}",
	configuration = CitizenIntegrationConfiguration.class)
interface CitizenClient {

	@GetMapping("/citizen/{personId}")
	Citizen getCitizen(@PathVariable("personId") final String personId);
}
