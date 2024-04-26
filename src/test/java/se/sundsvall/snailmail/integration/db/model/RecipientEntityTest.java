package se.sundsvall.snailmail.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEqualsExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCodeExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

class RecipientEntityTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(RecipientEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCodeExcluding("requestEntity"),
			hasValidBeanEqualsExcluding("requestEntity"),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		// Set values as variables
		final var id = 12L;
		final var request = RequestEntity.builder().build();
		final var careOf = "careOf";
		final var givenName = "givenName";
		final var lastName = "lastName";
		final var adress = "adress";
		final var postalCode = "postalCode";
		final var city = "city";


		final var recipient = RecipientEntity.builder()
			.withId(id)
			.withRequestEntity(request)
			.withCareOf(careOf)
			.withGivenName(givenName)
			.withLastName(lastName)
			.withAddress(adress)
			.withPostalCode(postalCode)
			.withCity(city)
			.build();

		assertThat(recipient).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(recipient.getId()).isEqualTo(id);
		assertThat(recipient.getRequestEntity()).isNotNull();
		assertThat(recipient.getCareOf()).isEqualTo(careOf);
		assertThat(recipient.getGivenName()).isEqualTo(givenName);
		assertThat(recipient.getLastName()).isEqualTo(lastName);
		assertThat(recipient.getAddress()).isEqualTo(adress);
		assertThat(recipient.getPostalCode()).isEqualTo(postalCode);
		assertThat(recipient.getCity()).isEqualTo(city);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(RecipientEntity.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new RecipientEntity()).hasAllNullFieldsOrProperties();
	}

}
