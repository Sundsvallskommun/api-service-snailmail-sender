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
		assertThat(properties.port()).isEqualTo(389);
		assertThat(properties.domain()).isEqualTo("WORKGROUP");
		assertThat(properties.username()).isEqualTo("someUsername");
		assertThat(properties.password()).isEqualTo("somePassword");
		assertThat(properties.share()).isEqualTo("/someShare/");
		assertThat(properties.connectTimeout()).isEqualTo(Duration.parse("PT5S"));
		assertThat(properties.responseTimeout()).isEqualTo(Duration.parse("PT10S"));

		assertThat(properties.jcifsProperties().getProperty("jcifs.smb.client.connTimeout")).isEqualTo("5000");
		assertThat(properties.jcifsProperties().getProperty("jcifs.smb.client.responseTimeout")).isEqualTo("10000");
	}
}
