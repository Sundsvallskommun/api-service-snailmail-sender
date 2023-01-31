package se.sundsvall.snailmail.api.model;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("junit")
class SendSnailMailRequestTest {

    @Test
    void testBuilder() {

        var request = SendSnailMailRequest.builder()
                .withAttachments(List.of(
                        SendSnailMailRequest.Attachment.builder()
                                .withName("someName")
                                .withContentType("someContentType")
                                .withContent("someContent").build()
                ))
                .build();


        assertThat(request).isNotNull();
        assertThat(request.getAttachments()).satisfies(attachments -> {
            assertThat(attachments).hasSize(1);
            assertThat(attachments).hasSize(1);
            assertThat(attachments.get(0).getName()).isEqualTo("someName");
            assertThat(attachments.get(0).getContent()).isEqualTo("someContent");
            assertThat(attachments.get(0).getContentType()).isEqualTo("someContentType");
        });
    }
}