package se.sundsvall.snailmail.integration.citizen;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

import generated.se.sundsvall.citizen.CitizenAddress;
import generated.se.sundsvall.citizen.CitizenExtended;

@ExtendWith(MockitoExtension.class)
class CitizenIntegrationTest {

	@Mock
	private CitizenClient citizenMock;

	@InjectMocks
	private CitizenIntegration citizenIntegration;

	@Test
	void getCitizen() {
		final var uuid = randomUUID();
		final var citizenExtended = buildCitizen(uuid);

		when(citizenMock.getCitizen(uuid.toString())).thenReturn(Optional.ofNullable(citizenExtended));

		final var response = citizenIntegration.getCitizen(uuid.toString());

		assertThat(response).isEqualTo(citizenExtended);

		verify(citizenMock).getCitizen(uuid.toString());
		verifyNoMoreInteractions(citizenMock);
	}

	@Test
	void testGetCitizenReturnsEmpty_shouldThrowException() {
		final var uuid = randomUUID();

		when(citizenMock.getCitizen(uuid.toString())).thenReturn(Optional.empty());

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> citizenIntegration.getCitizen(uuid.toString()))
			.satisfies(problem -> {
				assertThat(problem.getStatus()).isEqualTo(Status.INTERNAL_SERVER_ERROR);
				assertThat(problem.getDetail()).isEqualTo("Failed to fetch citizen data from Citizen API");
			});
	}

	@Test
	void getCitizenThrowingException() {
		final var uuid = randomUUID();
		final var citizenExtended = buildCitizen(uuid);

		when(citizenMock.getCitizen(uuid.toString()))
				.thenThrow(Problem.builder()
				.withStatus(Status.BAD_GATEWAY)
				.withCause(Problem.builder()
					.withStatus(Status.BAD_GATEWAY)
					.build())
				.build());

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> citizenIntegration.getCitizen(uuid.toString()))
			.satisfies(problem -> {
				assertThat(problem.getStatus()).isEqualTo(Status.BAD_GATEWAY);
				assertThat(problem.getCause()).isNotNull();
				assertThat(problem.getCause().getStatus()).isEqualTo(Status.BAD_GATEWAY);
			});

		verify(citizenMock).getCitizen(uuid.toString());
		verifyNoMoreInteractions(citizenMock);
	}

	private CitizenExtended buildCitizen(UUID uuid) {
		return new CitizenExtended()
			.givenname("someGivenName")
			.lastname("someLastName")
			.personId(uuid)
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
