package apptest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.snailmail.Application;
import se.sundsvall.snailmail.integration.db.BatchRepository;
import se.sundsvall.snailmail.integration.db.model.BatchEntity;
import se.sundsvall.snailmail.integration.db.model.DepartmentEntity;

@WireMockAppTestSuite(files = "classpath:/SnailMailIT/", classes = Application.class)
@Sql({"/db/scripts/truncate.sql", "/db/scripts/testdata-it.sql"})
@Testcontainers
class SnailMailIT extends AbstractAppTest {

	private static final int ORIGINAL_SAMBA_PORT = 445;

	@Container
	public static final GenericContainer<?> sambaContainer = new GenericContainer<>("dperson/samba")
		.withCommand("-s", "share;/share/;yes;no;no;user;none;user;none",
			"-u", "user;1234",
			"-w", "WORKGROUP",
			"-p")
		.withExposedPorts(ORIGINAL_SAMBA_PORT)
		//.waitingFor(Wait.forLogMessage(".*smbd version.*started.*", 1)
		//	.withStartupTimeout(Duration.ofSeconds(60)))
		.withStartupTimeout(Duration.ofSeconds(60))
		.withReuse(true);

	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("integration.samba.port", () -> sambaContainer.getMappedPort(ORIGINAL_SAMBA_PORT));
		registry.add("integration.samba.host", sambaContainer::getHost);
	}

	private static final String MUNICIPALITY_ID = "2281";
	private static final String REQUEST_FILE = "request.json";
	private static final String REQUEST_FILE2 = "request2.json";
	private static final String X_SENT_BY_HEADER = "X-Sent-By";
	private static final String X_SENT_BY_VALUE = "type=adAccount; joe01doe";

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
			.withHeader(X_SENT_BY_HEADER, X_SENT_BY_VALUE)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(OK)
			.sendRequest();

		setupCall()
			.withServicePath("/" + MUNICIPALITY_ID + "/send/snailmail")
			.withHttpMethod(POST)
			.withHeader(X_SENT_BY_HEADER, X_SENT_BY_VALUE)
			.withRequest(REQUEST_FILE2)
			.withExpectedResponseStatus(OK)
			.sendRequest();

		final var batchEntityList = batchRepository.findAll();
		
		assertThat(batchEntityList)
			.extracting(
				BatchEntity::getId,
				BatchEntity::getMunicipalityId,
				BatchEntity::getSentBy,
				batch -> batch.getDepartmentEntities().stream()
					.map(DepartmentEntity::getName)
					.toList())
			.containsExactlyInAnyOrder(
				tuple("123e4567-e89b-12d3-a456-426614174000", MUNICIPALITY_ID, "joe01doe", List.of("A Department")),
				tuple("58f96da8-6d76-4fa6-bb92-64f71fdc6aa7", MUNICIPALITY_ID, "joe01doe", List.of("Dummy Department")),
				tuple("c895f6b2-3571-413a-a2f4-8d7780d6c6a5", MUNICIPALITY_ID, "joe01doe", List.of("McDummy Department")),
				tuple("fa0cc3d7-5002-404b-8675-758598d4221d", MUNICIPALITY_ID, "joe01doe", List.of("Another Department"))
			);
	}
	
	@Test
	void test4_sendSnailMailWithAddress() {
		setupCall()
			.withServicePath("/" + MUNICIPALITY_ID + "/send/snailmail")
			.withHttpMethod(POST)
			.withHeader(X_SENT_BY_HEADER, X_SENT_BY_VALUE)
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
