package se.sundsvall.snailmail.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import se.sundsvall.snailmail.api.model.EnvelopeType;

class SnailMailDtoTest {

    @Test
    void testBuilder() {

        var request = SnailMailDto.builder()
                .withDeviation("someDeviation")
                .withDepartment("someDepartment")
                .withAttachments(List.of(
                        SnailMailDto.AttachmentDto.builder()
                                .withName("someName")
                                .withContent("someContent")
                                .withContentType("someContentType")
                                .withEnvelopeType(EnvelopeType.PLAIN)
                                .build()
                ))
                .withCitizenDto(CitizenDto.builder().build())
                .build();

        assertThat(request).isNotNull();
        assertThat(request.getCitizenDto()).isNotNull();
        assertThat(request.getDeviation()).isEqualTo("someDeviation");
        assertThat(request.getDepartment()).isEqualTo("someDepartment");
        assertThat(request.getAttachments()).satisfies(attachments -> {
            assertThat(attachments).hasSize(1);
            assertThat(attachments.getFirst().getName()).isEqualTo("someName");
            assertThat(attachments.getFirst().getContent()).isEqualTo("someContent");
            assertThat(attachments.getFirst().getContentType()).isEqualTo("someContentType");
            assertThat(attachments.getFirst().getEnvelopeType()).isEqualByComparingTo(EnvelopeType.PLAIN);
        });
    }
}
