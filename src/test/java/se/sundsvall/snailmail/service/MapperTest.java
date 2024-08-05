package se.sundsvall.snailmail.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import se.sundsvall.snailmail.api.model.EnvelopeType;
import se.sundsvall.snailmail.api.model.SendSnailMailRequest;
import se.sundsvall.snailmail.integration.db.model.BatchEntity;
import se.sundsvall.snailmail.integration.db.model.DepartmentEntity;
import se.sundsvall.snailmail.integration.db.model.RequestEntity;

import generated.se.sundsvall.citizen.CitizenAddress;
import generated.se.sundsvall.citizen.CitizenExtended;

class MapperTest {

	@Test
	void toRequestShouldMapCorrectly() {
		final var deviation = "deviation";
		final var sendSnailMailRequest = SendSnailMailRequest.builder().withDeviation(deviation).build();
		final var citizen = new CitizenExtended();
		final var department = new DepartmentEntity();

		final var result = Mapper.toRequest(sendSnailMailRequest, citizen, department);

		assertThat(result).isNotNull();
		assertThat(result.getDepartmentEntity()).isEqualTo(department);
		assertThat(result.getDeviation()).isEqualTo(deviation);
	}

	@Test
	void toAttachmentShouldMapCorrectly() {
		final var content = "content";
		final var name = "name";
		final var contentType = "contentType";
		final var envelopeType = EnvelopeType.WINDOWED;
		final var sendSnailMailRequestAttachment = SendSnailMailRequest.Attachment.builder()
			.withContent(content)
			.withName(name)
			.withContentType(contentType)
			.withEnvelopeType(envelopeType)
			.build();
		final var request = new RequestEntity();

		final var result = Mapper.toAttachment(sendSnailMailRequestAttachment, request);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).isEqualTo(content);
		assertThat(result.getName()).isEqualTo(name);
		assertThat(result.getContentType()).isEqualTo(contentType);
		assertThat(result.getEnvelopeType()).isEqualTo(envelopeType);
		assertThat(result.getRequestEntity()).isEqualTo(request);
	}

	@Test
	void toDepartmentShouldMapCorrectly() {
		final var departmentName = "DepartmentEntity Name";
		final var batch = new BatchEntity();

		final var result = Mapper.toDepartment(departmentName, batch);

		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(departmentName);
		assertThat(result.getBatchEntity()).isEqualTo(batch);
	}

	@Test
	void toRecipientShouldMapCorrectlyWhenCitizenIsNull() {

		final var result = Mapper.toRecipient(null);

		assertThat(result).isNull();
	}

	@Test
	void toRecipientShouldMapCorrectlyWhenCitizenHasNoAddresses() {
		final var citizen = new CitizenExtended()
			.addresses(Collections.emptyList());

		final var result = Mapper.toRecipient(citizen);

		assertThat(result).isNull();
	}

	@Test
	void toRecipientShouldMapCorrectlyWhenCitizenHasAddresses() {
		final var givenName = "John";
		final var lastName = "Doe";
		final var citizen = new CitizenExtended()
			.givenname(givenName)
			.lastname(lastName)
			.addresses(List.of(new CitizenAddress()));

		final var result = Mapper.toRecipient(citizen);

		assertThat(result).isNotNull();
		assertThat(result.getGivenName()).isEqualTo(givenName);
		assertThat(result.getLastName()).isEqualTo(lastName);
	}

	@Test
	void toRequestWhenRequestIsNull() {
		final var result = Mapper.toRequest(null, null, null);

		assertThat(result).isNull();
	}

	@Test
	void toAttachmentWhenAttachmentIsNull() {
		final var result = Mapper.toAttachment(null, null);

		assertThat(result).isNull();
	}

}
