package se.sundsvall.snailmail.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Validated
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "batch.unhandled")
public class BatchProperties {

	@NotBlank
	private String checkInterval;
	@NotBlank
	private String initialDelay;
	@NotBlank
	private String lockAtMostFor;
	@NotBlank
	private String name;
	@NotBlank
	private String outdatedAfter;
}
