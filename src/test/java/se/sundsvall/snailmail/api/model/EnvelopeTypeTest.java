package se.sundsvall.snailmail.api.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EnvelopeTypeTest {

	@Test
	void testEnumValues() {
		assertThat(EnvelopeType.values()).containsExactlyInAnyOrder(EnvelopeType.WINDOWED, EnvelopeType.PLAIN);
	}
}