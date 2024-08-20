package apptest;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
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
			.withExposedService("samba", 445, Wait.forListeningPort())
			.withStartupTimeout(Duration.ofSeconds(60));

	private static final String MUNICIPALITY_ID = "2281";

	@Autowired
	private BatchRepository batchRepository;

	@Test
	void test1_sendSnailMail() {
		setupCall()
			.withServicePath("/" + MUNICIPALITY_ID + "/send/snailmail")
			.withHttpMethod(HttpMethod.POST)
			.withRequest("request.json")
			.withExpectedResponseStatus(HttpStatus.OK)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test2_sendBatch() {
		setupCall()
			.withServicePath("/" + MUNICIPALITY_ID + "/send/batch/123e4567-e89b-12d3-a456-426614174000")
			.withHttpMethod(HttpMethod.POST)
			.withExpectedResponseStatus(HttpStatus.OK)
			.sendRequestAndVerifyResponse();
	}

	//Check that new batches with same department generates multiple batches
	@Test
	void test3_sendMultipleSnailMails() {

		setupCall()
			.withServicePath("/" + MUNICIPALITY_ID + "/send/snailmail")
			.withHttpMethod(HttpMethod.POST)
			.withRequest("request.json")
			.withExpectedResponseStatus(HttpStatus.OK)
			.sendRequest();

		setupCall()
			.withServicePath("/" + MUNICIPALITY_ID + "/send/snailmail")
			.withHttpMethod(HttpMethod.POST)
			.withRequest("request2.json")
			.withExpectedResponseStatus(HttpStatus.OK)
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
}
