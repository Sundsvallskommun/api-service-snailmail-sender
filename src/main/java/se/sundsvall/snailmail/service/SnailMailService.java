package se.sundsvall.snailmail.service;

import org.springframework.stereotype.Service;
import se.sundsvall.snailmail.api.model.SendSnailMailRequest;
import se.sundsvall.snailmail.integration.emailsender.EmailSenderIntegration;

@Service
public class SnailMailService {
    private final EmailSenderIntegration emailSenderIntegration;

    private final se.sundsvall.snailmail.service.Mapper mapper;

    public SnailMailService(EmailSenderIntegration emailSenderIntegration, Mapper mapper) {
        this.emailSenderIntegration = emailSenderIntegration;
        this.mapper = mapper;
    }

    public void sendSnailMail(SendSnailMailRequest request) {

        var mail = mapper.toSnailMailDto(request, null);
        emailSenderIntegration.sendEmail(mail);

    }

}
