package se.sundsvall.snailmail.integration.citizen;

import java.util.List;

import org.springframework.stereotype.Component;

import generated.se.sundsvall.citizen.CitizenExtended;

@Component
public class CitizenIntegration {

    static final String INTEGRATION_NAME = "Citizen";
    private final CitizenClient client;

    public CitizenIntegration(final CitizenClient client) {
        this.client = client;
    }

    /**
     * Get citizens by personIds
     * @param personIds list of personIds
     * @return a list of {@link CitizenExtended}
     */
    public List<CitizenExtended> getCitizens(final List<String> personIds) {
        return client.getCitizens(false, personIds);    //false to never fetch classified citizens
    }
}
