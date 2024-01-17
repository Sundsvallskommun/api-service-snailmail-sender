package apptest;

import static apptest.CommonStubs.stubForAccessToken;

import java.io.File;
import java.time.Duration;

import org.junit.jupiter.api.Test;
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

@WireMockAppTestSuite(files = "classpath:/SnailMailIT/", classes = Application.class)
@Sql({"/db/scripts/truncate.sql", "/db/scripts/testdata-it.sql"})
@Testcontainers
class SnailMailIT extends AbstractAppTest {

	@Container
	public static final DockerComposeContainer<?> sambaContainer =
		new DockerComposeContainer<>(new File("src/test/resources/docker/docker-compose.yml"))
			.withExposedService("samba", 445, Wait.forListeningPort())
			.withStartupTimeout(Duration.ofSeconds(60));

	@Test
	void test1_sendSnailMail() {
		stubForAccessToken();

		setupCall()
			.withServicePath("/send/snailmail")
			.withHttpMethod(HttpMethod.POST)
			.withRequest("request.json")
			.withExpectedResponseStatus(HttpStatus.OK)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test2_sendBatch() {

		setupCall()
			.withServicePath("/send/batch/123e4567-e89b-12d3-a456-426614174000")
			.withHttpMethod(HttpMethod.POST)
			.withExpectedResponseStatus(HttpStatus.OK)
			.sendRequestAndVerifyResponse();
	}

}
