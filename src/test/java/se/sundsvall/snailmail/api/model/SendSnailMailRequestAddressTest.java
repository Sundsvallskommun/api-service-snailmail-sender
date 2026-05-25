package se.sundsvall.snailmail.api.model;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

class SendSnailMailRequestAddressTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(SendSnailMailRequest.Address.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		final var address = SendSnailMailRequest.Address.builder()
			.withFirstName("John")
			.withLastName("Doe")
			.withOrganizationName("Acme AB")
			.withAddress("Main Street 1")
			.withApartmentNumber("1101")
			.withCareOf("c/o Jane Doe")
			.withZipCode("12345")
			.withCity("Sundsvall")
			.withCountry("Sweden")
			.withOrganizationNumber("123456-7890")
			.build();

		assertThat(address).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(address.getFirstName()).isEqualTo("John");
		assertThat(address.getLastName()).isEqualTo("Doe");
		assertThat(address.getOrganizationName()).isEqualTo("Acme AB");
		assertThat(address.getAddress()).isEqualTo("Main Street 1");
		assertThat(address.getApartmentNumber()).isEqualTo("1101");
		assertThat(address.getCareOf()).isEqualTo("c/o Jane Doe");
		assertThat(address.getZipCode()).isEqualTo("12345");
		assertThat(address.getCity()).isEqualTo("Sundsvall");
		assertThat(address.getCountry()).isEqualTo("Sweden");
		assertThat(address.getOrganizationNumber()).isEqualTo("123456-7890");
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(SendSnailMailRequest.Address.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new SendSnailMailRequest.Address()).hasAllNullFieldsOrProperties();
	}
}
