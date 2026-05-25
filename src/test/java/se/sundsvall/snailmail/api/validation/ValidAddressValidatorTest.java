package se.sundsvall.snailmail.api.validation;

import org.junit.jupiter.api.Test;
import se.sundsvall.snailmail.api.model.SendSnailMailRequest;

import static org.assertj.core.api.Assertions.assertThat;

class ValidAddressValidatorTest {

	private final ValidAddressValidator validator = new ValidAddressValidator();

	@Test
	void nullAddressIsValid() {
		assertThat(validator.isValid(null, null)).isTrue();
	}

	@Test
	void onlyFirstNameIsInvalid() {
		var address = SendSnailMailRequest.Address.builder()
			.withFirstName("John")
			.build();
		assertThat(validator.isValid(address, null)).isFalse();
	}

	@Test
	void onlyLastNameIsInvalid() {
		var address = SendSnailMailRequest.Address.builder()
			.withLastName("Doe")
			.build();
		assertThat(validator.isValid(address, null)).isFalse();
	}

	@Test
	void firstAndLastNameIsValid() {
		var address = SendSnailMailRequest.Address.builder()
			.withFirstName("John")
			.withLastName("Doe")
			.build();
		assertThat(validator.isValid(address, null)).isTrue();
	}

	@Test
	void onlyOrganizationNameIsValid() {
		var address = SendSnailMailRequest.Address.builder()
			.withOrganizationName("Acme AB")
			.build();
		assertThat(validator.isValid(address, null)).isTrue();
	}

	@Test
	void allThreeNamesIsValid() {
		var address = SendSnailMailRequest.Address.builder()
			.withFirstName("John")
			.withLastName("Doe")
			.withOrganizationName("Acme AB")
			.build();
		assertThat(validator.isValid(address, null)).isTrue();
	}

	@Test
	void noNamesIsInvalid() {
		var address = SendSnailMailRequest.Address.builder()
			.withAddress("Main Street 1")
			.withZipCode("12345")
			.withCity("Sundsvall")
			.build();
		assertThat(validator.isValid(address, null)).isFalse();
	}

	@Test
	void blankNamesIsInvalid() {
		var address = SendSnailMailRequest.Address.builder()
			.withFirstName("  ")
			.withLastName("  ")
			.withOrganizationName(" ")
			.build();
		assertThat(validator.isValid(address, null)).isFalse();
	}
}
