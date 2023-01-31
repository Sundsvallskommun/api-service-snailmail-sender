package se.sundsvall.snailmail.integration;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;

@Getter
@Setter
public abstract class AbstractIntegrationProperties {
    private String baseUrl;
    private Duration readTimeout = Duration.ofSeconds(15);
    private Duration connectTimeout = Duration.ofSeconds(5);
    private String tokenUri;
    private String clientId;
    private String clientSecret;
    private String grantType = "client_credentials";

}
