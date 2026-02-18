package se.sundsvall.snailmail.api.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EnvelopeTypeTest {

	@Test
	void testEnumValues() {
		assertThat(EnvelopeType.values()).containsExactlyInAnyOrder(EnvelopeType.WINDOWED, EnvelopeType.PLAIN);
	}
}
