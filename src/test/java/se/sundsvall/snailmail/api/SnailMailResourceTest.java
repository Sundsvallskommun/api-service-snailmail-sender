package se.sundsvall.snailmail.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.zalando.problem.Status.BAD_REQUEST;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;

import se.sundsvall.snailmail.Application;
import se.sundsvall.snailmail.api.model.SendSnailMailRequest;
import se.sundsvall.snailmail.service.SnailMailService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class SnailMailResourceTest {

	@MockBean
	private SnailMailService mockSnailMailService;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void sendSnailMail() {

		// Arrange
		final var issuer = "issuer";
		final var municipalityId = "2281";
		final var request = SendSnailMailRequest.builder()
			.withDepartment("department")
			.withPartyId(UUID.randomUUID().toString())
			.withBatchId(UUID.randomUUID().toString())
			.build();

		// ACT
		webTestClient.post()
			.uri("/2281/send/snailmail")
			.header("x-issuer", issuer)
			.bodyValue(request)
			.exchange()
			.expectStatus()
			.isOk();

		// VERIFY
		verify(mockSnailMailService).sendSnailMail(municipalityId, request, issuer);

	}

	@Test
	void sendBatch() {

		final var uuid = UUID.randomUUID().toString();
		final var municipalityId = "2281";

		// ACT
		webTestClient.post()
			.uri("/2281/send/batch/" + uuid)
			.exchange()
			.expectStatus()
			.isOk();

		verify(mockSnailMailService).sendBatch(municipalityId, uuid);

	}

	@Test
	void sendBatch_notUUID() {

		// ACT
		final var response = webTestClient.post()
			.uri("/2281/send/batch/abc")
			.exchange()
			.expectStatus()
			.isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// ASSERT
		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactlyInAnyOrder(tuple("sendBatch.batchId", "not a valid UUID"));

		verifyNoInteractions(mockSnailMailService);

	}

}
