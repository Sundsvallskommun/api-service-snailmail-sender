package se.sundsvall.snailmail.integration.citizen;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import generated.se.sundsvall.citizen.Citizen;
import generated.se.sundsvall.citizen.CitizenAddress;

@ExtendWith(MockitoExtension.class)
class CitizenIntegrationTest {

	@Mock
	private CitizenClient client;

	@InjectMocks
	private CitizenIntegration citizenIntegration;

	@Test
	void getCitizen() {
		final var citizen = buildCitizen();
		when(client.getCitizen(any())).thenReturn(citizen);

		final var response = citizenIntegration.getCitizen("someId");

		assertThat(response).isNotNull().usingRecursiveComparison().isEqualTo(citizen);

		verify(client).getCitizen(any(String.class));
		verifyNoMoreInteractions(client);
	}

	@Test
	void getCitizenThrowingException() {
		when(client.getCitizen(any())).thenThrow(new NullPointerException());
		final var response = citizenIntegration.getCitizen("someId");

		assertThat(response).isNull();

		verify(client).getCitizen(any(String.class));
		verifyNoMoreInteractions(client);

	}

	private Citizen buildCitizen() {
		return new Citizen()
			.givenname("someGivenName")
			.lastname("someLastName")
			.personId(randomUUID())
			.nrDate("someNrDate")
			.addresses(List.of(
				new CitizenAddress()
					.status("CURRENT")
					.address("someAdress")
					.addressNumber("someAdressNumber")
					.addressArea("someAdressArea")
					.co("someCo")
					.postalCode("somePostalCode")
					.city("someCity")
					.country("someCountry")));

	}
}
