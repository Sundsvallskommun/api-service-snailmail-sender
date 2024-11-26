package apptest;

import static org.assertj.core.api.Assertions.assertThat;

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
import se.sundsvall.snailmail.service.BatchScheduler;

@WireMockAppTestSuite(files = "classpath:/SnailMailIT/", classes = Application.class)
@Sql({"/db/scripts/truncate.sql", "/db/scripts/testdata-it.sql"})
@Testcontainers
class BatchSchedulingIT extends AbstractAppTest {

	@Container
	public static final DockerComposeContainer<?> sambaContainer =
		new DockerComposeContainer<>(new File("src/test/resources/docker/docker-compose.yml"))
			.withStartupTimeout(Duration.ofSeconds(60));

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
