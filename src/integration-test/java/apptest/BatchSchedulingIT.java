package apptest;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
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
import se.sundsvall.snailmail.service.BatchScheduler;

@WireMockAppTestSuite(files = "classpath:/SnailMailIT/", classes = Application.class)
@Sql({"/db/scripts/truncate.sql", "/db/scripts/testdata-it.sql"})
@Testcontainers
class BatchSchedulingIT extends AbstractAppTest {

	private static final int ORIGINAL_SAMBA_PORT = 445;

	@Container
	public static final GenericContainer<?> sambaContainer = new GenericContainer<>("dperson/samba")
		.withCommand("-s", "share;/share/;yes;no;no;user;none;user;none",
			"-u", "user;1234",
			"-w", "WORKGROUP",
			"-p")
		.withExposedPorts(ORIGINAL_SAMBA_PORT)
		.withStartupTimeout(Duration.ofSeconds(60))
		.withReuse(true);

	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("integration.samba.port", () -> sambaContainer.getMappedPort(ORIGINAL_SAMBA_PORT));
		registry.add("integration.samba.host", sambaContainer::getHost);
	}

	@Autowired
	private BatchScheduler batchScheduler;
	
	@Autowired
	private BatchRepository batchRepository;
	
	@Test
	void test1_unhandledBatches_shouldBeSent() {
		setupCall();
		var batches = batchRepository.findAll();
		assertThat(batches).hasSize(2);
		// Trigger batch job
		batchScheduler.sendUnhandledBatches();

		assertThat(batchRepository.findAll()).isEmpty();
	}
}
