package se.sundsvall.snailmail.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class RecipientTest {

	@Test
	void testBean() {
		assertThat(Recipient.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		// Set values as variables
		final var id = 12L;
		final var request = Request.builder().build();
		final var co = "co";
		final var givenName = "givenName";
		final var lastName = "lastName";
		final var adress = "adress";
		final var postalCode = "postalCode";
		final var city = "city";


		final var recipient = Recipient.builder()
			.withId(id)
			.withRequest(request)
			.withCo(co)
			.withGivenName(givenName)
			.withLastName(lastName)
			.withAdress(adress)
			.withPostalCode(postalCode)
			.withCity(city)
			.build();

		Assertions.assertThat(recipient).isNotNull().hasNoNullFieldsOrProperties();
		Assertions.assertThat(recipient.getId()).isEqualTo(id);
		Assertions.assertThat(recipient.getRequest()).isNotNull();
		Assertions.assertThat(recipient.getCo()).isEqualTo(co);
		Assertions.assertThat(recipient.getGivenName()).isEqualTo(givenName);
		Assertions.assertThat(recipient.getLastName()).isEqualTo(lastName);
		Assertions.assertThat(recipient.getAdress()).isEqualTo(adress);
		Assertions.assertThat(recipient.getPostalCode()).isEqualTo(postalCode);
		Assertions.assertThat(recipient.getCity()).isEqualTo(city);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		Assertions.assertThat(Recipient.builder().build()).hasAllNullFieldsOrProperties();
		Assertions.assertThat(new Recipient()).hasAllNullFieldsOrProperties();
	}

}
