package se.sundsvall.snailmail.integration.citizen;

import generated.se.sundsvall.citizen.Citizen;
import generated.se.sundsvall.citizen.CitizenAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CitizenIntegrationTest {

    @Mock
    private CitizenClient client;

    private CitizenIntegration citizenIntegration;
    private UUID uuid;


    @BeforeEach
    void setUp() {
        citizenIntegration = new CitizenIntegration(client);
        uuid = UUID.randomUUID();
    }

    @Test
    void getCitizen() {
        var citizen = buildCitizen();
        when(client.getCitizen(any())).thenReturn(citizen);

        var response = citizenIntegration.getCitizen("someId");

        assertThat(response).isNotNull();
        assertThat(response).isEqualTo(citizen).usingRecursiveComparison();

        verify(client, times(1)).getCitizen(any(String.class));
        verifyNoMoreInteractions(client);
    }


    @Test
    void getCitizenThrowingException() {
        when(client.getCitizen(any())).thenThrow(new NullPointerException());
        var response = citizenIntegration.getCitizen("someId");

        assertThat(response).isNull();

        verify(client, times(1)).getCitizen(any(String.class));
        verifyNoMoreInteractions(client);

    }

    private Citizen buildCitizen() {
        return new Citizen()
                .givenname("someGivenName")
                .lastname("someLastName")
                .personId(uuid)
                .nrDate("someNrDate")
                .addAddressesItem(new CitizenAddress()
                        .status("CURRENT")
                        .address("someAdress")
                        .addressNumber("someAdressNumber")
                        .addressArea("someAdressArea")
                        .co("someCo")
                        .postalCode("somePostalCode")
                        .city("someCity")
                        .country("someCountry"));

    }
}