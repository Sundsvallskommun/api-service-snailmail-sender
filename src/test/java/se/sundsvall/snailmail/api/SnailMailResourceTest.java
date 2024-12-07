package se.sundsvall.snailmail.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;
import se.sundsvall.snailmail.Application;
import se.sundsvall.snailmail.api.model.SendSnailMailRequest;
import se.sundsvall.snailmail.service.SnailMailService;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static org.zalando.problem.Status.BAD_REQUEST;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class SnailMailResourceTest {

	private static final String MUNICIPALITY_ID = "2281";

	@MockBean
	private SnailMailService mockSnailMailService;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void sendSnailMail() {
		var issuer = "issuer";
		var request = SendSnailMailRequest.builder()
			.withMunicipalityId(MUNICIPALITY_ID)
			.withIssuer(issuer)
			.withDepartment("department")
			.withBatchId(UUID.randomUUID().toString())
			.withAddress(SendSnailMailRequest.Address.builder().build())
			.withAttachments(List.of(SendSnailMailRequest.Attachment.builder()
				.withName("name")
				.withContent("Y29udGVudA==")
				.withContentType(APPLICATION_PDF_VALUE)
				.build()))
			.build();

		webTestClient.post()
			.uri("/%s/send/snailmail".formatted(MUNICIPALITY_ID))
			.header("x-issuer", issuer)
			.bodyValue(request)
			.exchange()
			.expectStatus()
			.isOk();

		verify(mockSnailMailService).sendSnailMail(request);
	}

	@Test
	void sendBatch() {
		var uuid = UUID.randomUUID().toString();

		webTestClient.post()
			.uri("/%s/send/batch/".formatted(MUNICIPALITY_ID) + uuid)
			.exchange()
			.expectStatus()
			.isOk();

		verify(mockSnailMailService).sendBatch(MUNICIPALITY_ID, uuid);
	}

	@Test
	void sendBatch_notUUID() {
		var response = webTestClient.post()
			.uri("/%s/send/batch/abc".formatted(MUNICIPALITY_ID))
			.exchange()
			.expectStatus()
			.isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactlyInAnyOrder(tuple("sendBatch.batchId", "not a valid UUID"));

		verifyNoInteractions(mockSnailMailService);
	}
}
