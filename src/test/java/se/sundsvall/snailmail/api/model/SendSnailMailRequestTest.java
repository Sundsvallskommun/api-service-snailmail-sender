package se.sundsvall.snailmail.api.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class SendSnailMailRequestTest {

	@Test
	void testBuilder() {

		final var request = SendSnailMailRequest.builder()
			.withAttachments(List.of(
				SendSnailMailRequest.Attachment.builder()
					.withName("someName")
					.withContentType("someContentType")
					.withContent("someContent")
					.withEnvelopeType(EnvelopeType.PLAIN)
				.build()))
			.build();

		assertThat(request).isNotNull();
		assertThat(request.getAttachments()).satisfies(attachments -> {
			assertThat(attachments).hasSize(1);
			assertThat(attachments).hasSize(1);
			assertThat(attachments.getFirst().getName()).isEqualTo("someName");
			assertThat(attachments.getFirst().getContent()).isEqualTo("someContent");
			assertThat(attachments.getFirst().getContentType()).isEqualTo("someContentType");
			assertThat(attachments.getFirst().getEnvelopeType()).isEqualByComparingTo(EnvelopeType.PLAIN);
		});
	}
}
