package se.sundsvall.snailmail.integration.samba;

import jakarta.validation.constraints.NotBlank;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("integration.samba")
public record SambaIntegrationProperties(

	@NotBlank String host,
	@DefaultValue("445") int port,
	@NotBlank String domain,
	@NotBlank String username,
	@NotBlank String password,
	@NotBlank String share,

	@DefaultValue("PT0.05S") Duration connectTimeout,
	@DefaultValue("PT0.05S") Duration responseTimeout) {}
