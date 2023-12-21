package se.sundsvall.snailmail.integration.samba;


import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.io.File;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import se.sundsvall.snailmail.Application;

//TODO Make a samba container work
@Testcontainers
@ActiveProfiles("junit")
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
class SambaIntegrationTest {

	@Container
	public static DockerComposeContainer<?> environment =
			new DockerComposeContainer<>(new File("src/test/resources/docker/docker-compose.yml"))
					.withExposedService("samba", 1445, Wait.forListeningPort())
					.withStartupTimeout(java.time.Duration.ofSeconds(60))
					.withLocalCompose(true);

	@Autowired
	private SambaIntegration sambaIntegration;

	@Test
	@Disabled
	void test() {
		Integer mappedPort = environment.getServicePort("samba_1", 1445);
		System.out.println(mappedPort);
		assertThat(sambaIntegration).isNotNull();
	}
}