package se.sundsvall.snailmail.integration.db.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import se.sundsvall.snailmail.api.model.EnvelopeType;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class AttachmentEntityTest {

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(AttachmentEntity.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new AttachmentEntity()).hasAllNullFieldsOrProperties();
	}

	@Test
	void testBuilderMethods() {
		var id = 12L;
		var request = RequestEntity.builder().build();
		var contentType = "contentType";
		var envelopeType = EnvelopeType.PLAIN;
		var name = "name";
		var content = "content";

		var attachment = AttachmentEntity.builder()
			.withId(id)
			.withRequestEntity(request)
			.withContentType(contentType)
			.withEnvelopeType(envelopeType)
			.withName(name)
			.withContent(content)
			.build();

		assertThat(attachment).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(attachment.getId()).isEqualTo(id);
		assertThat(attachment.getRequestEntity()).isNotNull();
		assertThat(attachment.getContentType()).isEqualTo(contentType);
		assertThat(attachment.getEnvelopeType()).isEqualTo(envelopeType);
		assertThat(attachment.getName()).isEqualTo(name);
		assertThat(attachment.getContent()).isEqualTo(content);
	}

	@ParameterizedTest
	@ArgumentsSource(EqualsArgumentsProvider.class)
	void testEquals(final Object first, final Object second, final boolean shouldEqual) {
		if (shouldEqual) {
			assertThat(first).isEqualTo(second);
		} else {
			assertThat(first).isNotEqualTo(second);
		}
	}

	@Test
	void testHashCode() {
		assertThat(AttachmentEntity.builder().build().hashCode()).isEqualTo(AttachmentEntity.class.hashCode());
	}

	private static class EqualsArgumentsProvider implements ArgumentsProvider {

		@Override
		public Stream<? extends Arguments> provideArguments(final ExtensionContext context) {
			var first = AttachmentEntity.builder().build();
			var second = AttachmentEntity.builder().build();

			return Stream.of(
				Arguments.of(first, second, false),
				Arguments.of(first, first, true),
				Arguments.of(first, "someString", false),
				Arguments.of(first.withId(123L), second.withId(123L), true),
				Arguments.of(first.withId(123L).withName("first"), second.withId(123L).withName("second"), true));
		}
	}
}
