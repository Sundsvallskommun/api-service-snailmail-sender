package se.sundsvall.snailmail.integration.db.model;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static org.assertj.core.api.Assertions.assertThat;

class RecipientEntityTest {

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(RecipientEntity.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new RecipientEntity()).hasAllNullFieldsOrProperties();
	}

	@Test
	void testBuilderMethods() {
		var id = 12L;
		var request = RequestEntity.builder().build();
		var careOf = "careOf";
		var givenName = "givenName";
		var lastName = "lastName";
		var address = "address";
		var apartmentNumber = "apartmentNumber";
		var postalCode = "postalCode";
		var city = "city";

		var recipient = RecipientEntity.builder()
			.withId(id)
			.withRequestEntity(request)
			.withCareOf(careOf)
			.withGivenName(givenName)
			.withLastName(lastName)
			.withAddress(address)
			.withApartmentNumber(apartmentNumber)
			.withPostalCode(postalCode)
			.withCity(city)
			.build();

		assertThat(recipient).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(recipient.getId()).isEqualTo(id);
		assertThat(recipient.getRequestEntity()).isNotNull();
		assertThat(recipient.getCareOf()).isEqualTo(careOf);
		assertThat(recipient.getGivenName()).isEqualTo(givenName);
		assertThat(recipient.getLastName()).isEqualTo(lastName);
		assertThat(recipient.getAddress()).isEqualTo(address);
		assertThat(recipient.getApartmentNumber()).isEqualTo(apartmentNumber);
		assertThat(recipient.getPostalCode()).isEqualTo(postalCode);
		assertThat(recipient.getCity()).isEqualTo(city);
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
			var first = RecipientEntity.builder().build();
			var second = RecipientEntity.builder().build();

			return Stream.of(
				Arguments.of(first, second, false),
				Arguments.of(first, first, true),
				Arguments.of(first, "someString", false),
				Arguments.of(first.withId(123L), second.withId(123L), true),
				Arguments.of(first.withId(123L).withAddress("first"), second.withId(123L).withAddress("second"), true));
		}
	}
}
