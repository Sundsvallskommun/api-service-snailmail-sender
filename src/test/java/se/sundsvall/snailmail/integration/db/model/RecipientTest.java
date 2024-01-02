package se.sundsvall.snailmail.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

class RecipientTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(Recipient.class, allOf(
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

		assertThat(recipient).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(recipient.getId()).isEqualTo(id);
		assertThat(recipient.getRequest()).isNotNull();
		assertThat(recipient.getCo()).isEqualTo(co);
		assertThat(recipient.getGivenName()).isEqualTo(givenName);
		assertThat(recipient.getLastName()).isEqualTo(lastName);
		assertThat(recipient.getAdress()).isEqualTo(adress);
		assertThat(recipient.getPostalCode()).isEqualTo(postalCode);
		assertThat(recipient.getCity()).isEqualTo(city);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(Recipient.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new Recipient()).hasAllNullFieldsOrProperties();
	}

}
