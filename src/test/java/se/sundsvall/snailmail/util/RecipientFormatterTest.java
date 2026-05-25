package se.sundsvall.snailmail.util;

import org.junit.jupiter.api.Test;
import se.sundsvall.snailmail.integration.db.model.RecipientEntity;

import static org.assertj.core.api.Assertions.assertThat;

class RecipientFormatterTest {

	@Test
	void personOnly() {
		final var recipient = RecipientEntity.builder()
			.withGivenName("John")
			.withLastName("Doe")
			.build();

		assertThat(RecipientFormatter.formatName(recipient)).isEqualTo("John Doe");
	}

	@Test
	void organizationOnly() {
		final var recipient = RecipientEntity.builder()
			.withOrganizationName("Acme AB")
			.build();

		assertThat(RecipientFormatter.formatName(recipient)).isEqualTo("Acme AB");
	}

	@Test
	void organizationAndPerson() {
		final var recipient = RecipientEntity.builder()
			.withGivenName("John")
			.withLastName("Doe")
			.withOrganizationName("Acme AB")
			.build();

		assertThat(RecipientFormatter.formatName(recipient)).isEqualTo("Acme AB (att: John Doe)");
	}

	@Test
	void blankOrganizationFallsBackToPerson() {
		final var recipient = RecipientEntity.builder()
			.withGivenName("John")
			.withLastName("Doe")
			.withOrganizationName("  ")
			.build();

		assertThat(RecipientFormatter.formatName(recipient)).isEqualTo("John Doe");
	}
}
