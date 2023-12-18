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
	void getCitizens() {
		final var uuid1 = randomUUID();
		final var uuid2 = randomUUID();
		final var citizen1 = buildCitizen(uuid1);
		final var citizen2 = buildCitizen(uuid2);

		when(citizenMock.getCitizens(eq(false), anyList())).thenReturn(List.of(citizen1, citizen2));

		final var response = citizenIntegration.getCitizens(List.of(uuid1.toString(), uuid2.toString()));

		assertThat(response)
				.hasSize(2)
				.extracting(CitizenExtended::getPersonId)
				.containsExactly(uuid1, uuid2);

		verify(citizenMock).getCitizens(false, List.of(uuid1.toString(), uuid2.toString()));
		verifyNoMoreInteractions(citizenMock);
	}

	@Test
	void getCitizenThrowingException() {
		final var uuid1 = randomUUID();
		final var uuid2 = randomUUID();
		final var citizen1 = buildCitizen(uuid1);
		final var citizen2 = buildCitizen(uuid2);

		when(citizenMock.getCitizens(eq(false), anyList()))
				.thenThrow(Problem.builder()
				.withStatus(Status.BAD_GATEWAY)
				.withCause(Problem.builder()
						.withStatus(Status.BAD_GATEWAY)
						.build())
				.build());

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> citizenIntegration.getCitizens(List.of(uuid1.toString(), uuid2.toString())))
			.satisfies(problem -> {
				assertThat(problem.getStatus()).isEqualTo(Status.BAD_GATEWAY);
				assertThat(problem.getCause()).isNotNull();
				assertThat(problem.getCause().getStatus()).isEqualTo(Status.BAD_GATEWAY);
			});

		verify(citizenMock).getCitizens(false, List.of(uuid1.toString(), uuid2.toString()));
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
