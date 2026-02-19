package se.sundsvall.snailmail.service;

import org.junit.jupiter.api.Test;
import se.sundsvall.snailmail.api.model.EnvelopeType;
import se.sundsvall.snailmail.api.model.SendSnailMailRequest;
import se.sundsvall.snailmail.integration.db.model.BatchEntity;
import se.sundsvall.snailmail.integration.db.model.DepartmentEntity;
import se.sundsvall.snailmail.integration.db.model.RecipientEntity;
import se.sundsvall.snailmail.integration.db.model.RequestEntity;

import static org.assertj.core.api.Assertions.assertThat;

class MapperTest {

	@Test
	void toRequestShouldMapCorrectly() {
		var deviation = "deviation";
		var sendSnailMailRequest = SendSnailMailRequest.builder().withDeviation(deviation).build();
		var recipient = new RecipientEntity();
		var department = new DepartmentEntity();

		var result = Mapper.toRequest(sendSnailMailRequest, recipient, department);

		assertThat(result).isNotNull();
		assertThat(result.getDepartmentEntity()).isEqualTo(department);
		assertThat(result.getDeviation()).isEqualTo(deviation);
	}

	@Test
	void toAttachmentShouldMapCorrectly() {
		var content = "content";
		var name = "name";
		var contentType = "contentType";
		var envelopeType = EnvelopeType.WINDOWED;
		var sendSnailMailRequestAttachment = SendSnailMailRequest.Attachment.builder()
			.withContent(content)
			.withName(name)
			.withContentType(contentType)
			.withEnvelopeType(envelopeType)
			.build();
		var request = new RequestEntity();

		var result = Mapper.toAttachment(sendSnailMailRequestAttachment, request);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).isEqualTo(content);
		assertThat(result.getName()).isEqualTo(name);
		assertThat(result.getContentType()).isEqualTo(contentType);
		assertThat(result.getEnvelopeType()).isEqualTo(envelopeType);
		assertThat(result.getRequestEntity()).isEqualTo(request);
	}

	@Test
	void toDepartmentShouldMapCorrectly() {
		var departmentName = "DepartmentEntity Name";
		var folderName = "Folder Name";
		var batch = new BatchEntity();

		var result = Mapper.toDepartment(departmentName, folderName, batch);

		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(departmentName);
		assertThat(result.getFolderName()).isEqualTo(folderName);
		assertThat(result.getBatchEntity()).isEqualTo(batch);
	}

	@Test
	void toRecipientShouldMapCorrectlyForNullAddress() {
		var result = Mapper.toRecipient(null);

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
	void toRequestWhenRequestIsNull() {
		var result = Mapper.toRequest(null, null, null);

		assertThat(result).isNull();
	}

	@Test
	void toAttachmentWhenAttachmentIsNull() {
		var result = Mapper.toAttachment(null, null);

		assertThat(result).isNull();
	}
}
