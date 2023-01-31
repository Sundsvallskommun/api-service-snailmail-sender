package se.sundsvall.snailmail.integration.emailsender;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import se.sundsvall.snailmail.integration.AbstractIntegrationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "integration.email-sender")
public class EmailSenderIntegrationProperties extends AbstractIntegrationProperties {
    private String emailAddress;
    private Sender sender;
    private String environment;

    @Getter
    @Setter
    public static class Sender {
        private String name;
        private String address;
        private String replyTo;

    }

}
