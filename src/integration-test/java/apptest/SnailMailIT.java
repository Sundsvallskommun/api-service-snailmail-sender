package apptest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;

import java.io.File;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.snailmail.Application;
import se.sundsvall.snailmail.integration.db.BatchRepository;

@WireMockAppTestSuite(files = "classpath:/SnailMailIT/", classes = Application.class)
@Sql({"/db/scripts/truncate.sql", "/db/scripts/testdata-it.sql"})
@Testcontainers
class SnailMailIT extends AbstractAppTest {

	@Container
	public static final DockerComposeContainer<?> sambaContainer =
		new DockerComposeContainer<>(new File("src/test/resources/docker/docker-compose.yml"))
			.withStartupTimeout(Duration.ofSeconds(60));

	private static final String MUNICIPALITY_ID = "2281";
	private static final String REQUEST_FILE = "request.json";
	private static final String REQUEST_FILE2 = "request2.json";

	@Autowired
	private BatchRepository batchRepository;

	@Test
	void test1_sendSnailMail() {
		setupCall()
			.withServicePath("/" + MUNICIPALITY_ID + "/send/snailmail")
			.withHttpMethod(POST)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(OK)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test2_sendBatch() {
		setupCall()
			.withServicePath("/" + MUNICIPALITY_ID + "/send/batch/123e4567-e89b-12d3-a456-426614174000")
			.withHttpMethod(POST)
			.withExpectedResponseStatus(OK)
			.sendRequestAndVerifyResponse();
	}

	//Check that new batches with same department generates multiple batches
	@Test
	void test3_sendMultipleSnailMails() {

		setupCall()
			.withServicePath("/" + MUNICIPALITY_ID + "/send/snailmail")
			.withHttpMethod(POST)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(OK)
			.sendRequest();

		setupCall()
			.withServicePath("/" + MUNICIPALITY_ID + "/send/snailmail")
			.withHttpMethod(POST)
			.withRequest(REQUEST_FILE2)
			.withExpectedResponseStatus(OK)
			.sendRequest();

		final var batchEntityList = batchRepository.findAll();

		//Verify that two separate batches has been created
		assertThat(batchEntityList.stream()
			.anyMatch(entity -> entity.getId().equalsIgnoreCase("58f96da8-6d76-4fa6-bb92-64f71fdc6aa7")))
			.isTrue();
		assertThat(batchEntityList.stream()
			.anyMatch(entity -> entity.getId().equalsIgnoreCase("c895f6b2-3571-413a-a2f4-8d7780d6c6a5")))
			.isTrue();
		//Also verify that they have the same department
		assertThat(batchEntityList.stream()
			.anyMatch(entity -> entity.getDepartmentEntities().stream()
				.allMatch(departmentEntity -> departmentEntity.getName().equalsIgnoreCase("dummy"))))
			.isTrue();
	}

	@Test
	void test4_sendSnailMailWithIssuer() {
		setupCall()
			.withServicePath("/" + MUNICIPALITY_ID + "/send/snailmail")
			.withHttpMethod(POST)
			.withHeader("x-issuer", "issuer")
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(OK)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test5_sendSnailMailWithAddress() {
		setupCall()
			.withServicePath("/" + MUNICIPALITY_ID + "/send/snailmail")
			.withHttpMethod(POST)
			.withHeader("x-issuer", "issuer")
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(OK)
			.sendRequestAndVerifyResponse();

		var batch = batchRepository.findById("12a96da8-6d76-4fa6-bb92-64f71fdc6aa6").orElseThrow();
		var department = batch.getDepartmentEntities().stream()
			.filter(dept44 -> dept44.getName().equals("test5_dummy_department"))
			.findFirst()
			.orElseThrow();
		var request = department.getRequestEntities().getFirst();

		assertThat(request.getRecipientEntity()).satisfies(recipient -> {
			assertThat(recipient.getGivenName()).isEqualTo("John");
			assertThat(recipient.getLastName()).isEqualTo("Doe");
			assertThat(recipient.getCity()).isEqualTo("Test-Town");
			assertThat(recipient.getApartmentNumber()).isEqualTo("1101");
			assertThat(recipient.getAddress()).isEqualTo("Test Street 123");
			assertThat(recipient.getCareOf()).isEqualTo("Johnny Doe");
			assertThat(recipient.getPostalCode()).isEqualTo("12345");
		});
	}
}
