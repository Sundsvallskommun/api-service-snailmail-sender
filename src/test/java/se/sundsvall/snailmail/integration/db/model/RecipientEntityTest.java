package se.sundsvall.snailmail.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEqualsExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCodeExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToStringExcluding;
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
			hasValidBeanToStringExcluding("requestEntity")));
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

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(RecipientEntity.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new RecipientEntity()).hasAllNullFieldsOrProperties();
	}
}
