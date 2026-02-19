package se.sundsvall.snailmail.integration.db.model;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static org.assertj.core.api.Assertions.assertThat;

class RequestEntityTest {

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(RequestEntity.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new RequestEntity()).hasAllNullFieldsOrProperties();
	}

	@Test
	void testBuilderMethods() {
		var id = 12L;
		var department = DepartmentEntity.builder().build();
		var recipient = RecipientEntity.builder().build();
		var attachment = AttachmentEntity.builder().build();
		var deviation = "deviation";
		var partyId = "partyId";

		var request = RequestEntity.builder()
			.withId(id)
			.withDepartmentEntity(department)
			.withRecipientEntity(recipient)
			.withAttachmentEntities(List.of(attachment))
			.withDeviation(deviation)
			.withPartyId(partyId)
			.build();

		assertThat(request).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(request.getId()).isEqualTo(id);
		assertThat(request.getDepartmentEntity()).isNotNull();
		assertThat(request.getRecipientEntity()).isNotNull();
		assertThat(request.getAttachmentEntities()).hasSize(1);
		assertThat(request.getDeviation()).isEqualTo(deviation);
		assertThat(request.getPartyId()).isEqualTo(partyId);
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
			var first = RequestEntity.builder().build();
			var second = RequestEntity.builder().build();

			return Stream.of(
				Arguments.of(first, second, false),
				Arguments.of(first, first, true),
				Arguments.of(first, "someString", false),
				Arguments.of(first.withId(123L), second.withId(123L), true),
				Arguments.of(first.withId(123L).withDeviation("first"), second.withId(123L).withDeviation("second"), true));
		}
	}
}
