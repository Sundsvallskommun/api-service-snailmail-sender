package se.sundsvall.snailmail.integration.emailsender;


import generated.se.sundsvall.emailsender.SendEmailRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
        name = EmailSenderIntegration.INTEGRATION_NAME,
        url = "${integration.email-sender.base-url}",
        configuration = EmailSenderIntegrationConfiguration.class
)
interface EmailSenderClient {

    @PostMapping("/send/email")
    ResponseEntity<Void> sendEmail(SendEmailRequest request);
}
