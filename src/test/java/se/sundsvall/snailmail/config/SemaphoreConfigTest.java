package se.sundsvall.snailmail.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.snailmail.Application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ActiveProfiles("junit")
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
class SemaphoreConfigTest {

	@Autowired
	private SemaphoreConfig semaphoreConfig;

	@Test
	void testSemaphoreConfig() {
		var semaphore = semaphoreConfig.semaphore();
		assertThat(semaphore).isNotNull();
		assertThat(semaphore.availablePermits()).isEqualTo(1);
	}

	@Test
	void testSemaphoreConfigSingleton() {
		var semaphore1 = semaphoreConfig.semaphore();
		var semaphore2 = semaphoreConfig.semaphore();
		assertThat(semaphore1).isSameAs(semaphore2);
	}
}
