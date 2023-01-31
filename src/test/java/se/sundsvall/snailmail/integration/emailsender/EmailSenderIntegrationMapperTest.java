package se.sundsvall.snailmail.integration.emailsender;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.snailmail.TestDataFactory.buildEmailProperties;
import static se.sundsvall.snailmail.TestDataFactory.buildSnailMailDto;
import static se.sundsvall.snailmail.TestDataFactory.buildSnailMailDtoWithBlankDeviation;
import static se.sundsvall.snailmail.TestDataFactory.buildSnailMailDtoWithoutDeviation;


@ExtendWith(MockitoExtension.class)
class EmailSenderIntegrationMapperTest {

    EmailSenderIntegrationProperties properties;

    EmailSenderIntegrationMapper mapper;

    @BeforeEach
    void setUp() {

        properties = buildEmailProperties();

        mapper = new EmailSenderIntegrationMapper(properties);
    }

    @Test
    void toSendEmailRequest() {
        var dto = buildSnailMailDto();

        var response = mapper.toSendEmailRequest(dto);

        assertThat(response).isNotNull();
        assertThat(response.getSubject()).isEqualTo("Utgående post - someDepartment - someDeviation - someoneToReplyTo");
        assertThat(response.getEmailAddress()).isEqualTo("some@email.se");
        assertThat(response.getSender().getName()).isEqualTo("someName");
        assertThat(response.getSender().getAddress()).isEqualTo("someemail@host.se");
        assertThat(response.getSender().getReplyTo()).isEqualTo("someoneToReplyTo");


    }

    @Test
    void toSendEmailRequestWithBlankDeviation() {
        var dto = buildSnailMailDtoWithBlankDeviation();
        var response = mapper.toSendEmailRequest(dto);
        assertThat(response).isNotNull();
        assertThat(response.getSubject()).isEqualTo("Utgående post - someDepartment - someoneToReplyTo");
        assertThat(response.getEmailAddress()).isEqualTo("some@email.se");
        assertThat(response.getSender().getName()).isEqualTo("someName");
        assertThat(response.getSender().getAddress()).isEqualTo("someemail@host.se");
        assertThat(response.getSender().getReplyTo()).isEqualTo("someoneToReplyTo");

    }

    @Test
    void toSendEmailRequestWithoutDeviation() {
        var dto = buildSnailMailDtoWithoutDeviation();
        var response = mapper.toSendEmailRequest(dto);
        assertThat(response).isNotNull();
        assertThat(response.getSubject()).isEqualTo("Utgående post - someDepartment - someoneToReplyTo");
        assertThat(response.getEmailAddress()).isEqualTo("some@email.se");
        assertThat(response.getSender().getName()).isEqualTo("someName");
        assertThat(response.getSender().getAddress()).isEqualTo("someemail@host.se");
        assertThat(response.getSender().getReplyTo()).isEqualTo("someoneToReplyTo");

    }
}