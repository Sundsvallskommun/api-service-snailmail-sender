package se.sundsvall.snailmail.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import se.sundsvall.snailmail.api.model.EnvelopeType;
import se.sundsvall.snailmail.api.model.SendSnailMailRequest;
import se.sundsvall.snailmail.integration.db.model.BatchEntity;
import se.sundsvall.snailmail.integration.db.model.DepartmentEntity;
import se.sundsvall.snailmail.integration.db.model.RecipientEntity;
import se.sundsvall.snailmail.integration.db.model.RequestEntity;

import generated.se.sundsvall.citizen.CitizenAddress;
import generated.se.sundsvall.citizen.CitizenExtended;

class MapperTest {

	@Test
	void toRequestShouldMapCorrectly() {
		final var deviation = "deviation";
		final var sendSnailMailRequest = SendSnailMailRequest.builder().withDeviation(deviation).build();
		final var recipient = new RecipientEntity();
		final var department = new DepartmentEntity();

		final var result = Mapper.toRequest(sendSnailMailRequest, recipient, department);

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
	void toRecipientShouldMapCorrectlyForNullAddress() {
		var result = Mapper.toRecipient((SendSnailMailRequest.Address) null);

		assertThat(result).isNull();
	}

	@Test
	void toRecipientShouldMapCorrectlyForAddress() {
		var address = SendSnailMailRequest.Address.builder()
			.withFirstName("givenName")
			.withLastName("lastName")
			.withAddress("address")
			.withApartmentNumber("apartmentNumber")
			.withZipCode("postalCode")
			.withCity("city")
			.build();

		var result = Mapper.toRecipient(address);

		assertThat(result).isNotNull();
		assertThat(result.getGivenName()).isEqualTo(address.getFirstName());
		assertThat(result.getLastName()).isEqualTo(address.getLastName());
		assertThat(result.getAddress()).isEqualTo(address.getAddress());
		assertThat(result.getApartmentNumber()).isEqualTo(address.getApartmentNumber());
		assertThat(result.getPostalCode()).isEqualTo(address.getZipCode());
		assertThat(result.getCity()).isEqualTo(address.getCity());
	}

	@Test
	void toRecipientShouldMapCorrectlyWhenCitizenIsNull() {

		final var result = Mapper.toRecipient((CitizenExtended) null);

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
