package se.sundsvall.snailmail.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
            assertThat(attachments.get(0).getName()).isEqualTo("someName");
            assertThat(attachments.get(0).getContent()).isEqualTo("someContent");
            assertThat(attachments.get(0).getContentType()).isEqualTo("someContentType");
        });
    }
}