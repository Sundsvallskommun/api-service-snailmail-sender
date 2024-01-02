package se.sundsvall.snailmail.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import se.sundsvall.snailmail.TestDataFactory;
import se.sundsvall.snailmail.api.model.EnvelopeType;

class MapperTest {

	@Test
	void testToSnailMailDto_withPopulatedCitizenObjectAndAttachment() {
		var snailMailDto = Mapper.toSnailMailDto(TestDataFactory.buildSendSnailMailRequest(), TestDataFactory.buildCitizenExtended());

		assertThat(snailMailDto.getBatchId()).isEqualTo("someBatchId");
		assertThat(snailMailDto.getDepartment()).isEqualTo("someDepartment");
		assertThat(snailMailDto.getDeviation()).isEqualTo("someDeviation");

		assertThat(snailMailDto.getAttachments()).hasSize(1);
		assertThat(snailMailDto.getAttachments().getFirst().getContent()).isEqualTo("base64");
		assertThat(snailMailDto.getAttachments().getFirst().getName()).isEqualTo("filename.pdf");
		assertThat(snailMailDto.getAttachments().getFirst().getContentType()).isEqualTo("application/pdf");
		assertThat(snailMailDto.getAttachments().getFirst().getEnvelopeType()).isEqualTo(EnvelopeType.PLAIN);

		assertThat(snailMailDto.getCitizenDto()).isNotNull();
		assertThat(UUID.fromString(snailMailDto.getCitizenDto().getPartyId())).isNotNull();
		assertThat(snailMailDto.getCitizenDto().getGivenName()).isEqualTo("Kalle");
		assertThat(snailMailDto.getCitizenDto().getLastName()).isEqualTo("Anka");
		assertThat(snailMailDto.getCitizenDto().getStreet()).isEqualTo("Ankeborgsvägen 1");
		assertThat(snailMailDto.getCitizenDto().getApartment()).isEqualTo("LGH 123");
		assertThat(snailMailDto.getCitizenDto().getCareOf()).isEqualTo("Kajsa Anka");
		assertThat(snailMailDto.getCitizenDto().getCity()).isEqualTo("ANKEBORG");
		assertThat(snailMailDto.getCitizenDto().getPostalCode()).isEqualTo("123 45");
	}

	@Test
	void testToSnailMailDto_withEmptyCitizenObject_shouldHaveEmptyCitizenDto() {
		//No need to test it all again, only verify citizenDto is empty.
		var snailMailDto = Mapper.toSnailMailDto(TestDataFactory.buildSendSnailMailRequest(), null);
		assertThat(snailMailDto.getCitizenDto()).isNotNull();
	}
}