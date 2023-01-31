package se.sundsvall.snailmail.integration.citizen;

import generated.se.sundsvall.citizen.Citizen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CitizenIntegration {

    static final String INTEGRATION_NAME = "Citizen";
    private static final Logger LOG = LoggerFactory.getLogger(CitizenIntegration.class);
    private final CitizenClient client;

    public CitizenIntegration(final CitizenClient client) {
        this.client = client;
    }

    public Citizen getCitizen(final String personId) {
        try {
            return client.getCitizen(personId);
        } catch (Exception e) {
            LOG.info("Unable to get citizen", e);
            return null;
        }
    }

}
