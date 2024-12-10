package se.sundsvall.snailmail.integration.samba;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.snailmail.Application;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class SambaIntegrationPropertiesTest {

	@Autowired
	private SambaIntegrationProperties properties;

	@Test
	void test() {
		assertThat(properties.host()).isEqualTo("localhost");
		assertThat(properties.port()).isEqualTo(1445);
		assertThat(properties.domain()).isEqualTo("WORKGROUP");
		assertThat(properties.username()).isEqualTo("user");
		assertThat(properties.password()).isEqualTo("1234");
		assertThat(properties.share()).isEqualTo("/share/");
		assertThat(properties.connectTimeout()).isEqualTo(Duration.parse("PT5S"));
		assertThat(properties.responseTimeout()).isEqualTo(Duration.parse("PT10S"));
	}

}
