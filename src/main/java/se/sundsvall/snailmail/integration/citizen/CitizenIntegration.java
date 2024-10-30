package se.sundsvall.snailmail.integration.citizen;

import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.zalando.problem.Problem;

import generated.se.sundsvall.citizen.CitizenExtended;

@Component
public class CitizenIntegration {

	private static final Logger log = LoggerFactory.getLogger(CitizenIntegration.class);

	private final CitizenClient client;

	public CitizenIntegration(final CitizenClient client) {
		this.client = client;
	}

	/**
	 * Fetch a citizen by partyId
	 *
	 * @param  partyId the partyId
	 * @return         a {@link CitizenExtended}
	 */
	public CitizenExtended getCitizen(final String partyId) {
		log.info("Fetching citizen data for partyId {}", partyId);
		return client.getCitizen(partyId)
			.orElseThrow(() -> Problem.builder()
				.withTitle("No citizen data found")
				.withStatus(INTERNAL_SERVER_ERROR)
				.withDetail("Failed to fetch citizen data from Citizen API")
				.build());
	}

}
