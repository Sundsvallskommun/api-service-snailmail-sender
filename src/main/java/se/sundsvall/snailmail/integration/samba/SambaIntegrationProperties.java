package se.sundsvall.snailmail.integration.samba;

import java.time.Duration;
import java.util.Properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import jakarta.validation.constraints.NotBlank;

@ConfigurationProperties("integration.samba")
public record SambaIntegrationProperties(

		@NotBlank
		String host,
		@DefaultValue("445")
		int port,
		@NotBlank
		String domain,
		@NotBlank
		String username,
		@NotBlank
		String password,
		@NotBlank
		String share,

		@DefaultValue("PT0.05S")
		Duration connectTimeout,
		@DefaultValue("PT0.05S")
		Duration responseTimeout
) {

	Properties jcifsProperties() {
		var jcifsProperties = new Properties();
		jcifsProperties.setProperty("jcifs.smb.client.connTimeout", Long.toString(connectTimeout().toMillis()));
		jcifsProperties.setProperty("jcifs.smb.client.responseTimeout", Long.toString(responseTimeout().toMillis()));
		return jcifsProperties;
	}
}
