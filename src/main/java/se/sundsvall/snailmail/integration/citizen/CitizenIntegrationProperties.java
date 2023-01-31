package se.sundsvall.snailmail.integration.citizen;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import se.sundsvall.snailmail.integration.AbstractIntegrationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "integration.citizen")
class CitizenIntegrationProperties extends AbstractIntegrationProperties {

}
