package se.sundsvall.snailmail.service;

import java.util.List;
import java.util.Optional;

import se.sundsvall.snailmail.api.model.SendSnailMailRequest;
import se.sundsvall.snailmail.integration.db.model.AttachmentEntity;
import se.sundsvall.snailmail.integration.db.model.BatchEntity;
import se.sundsvall.snailmail.integration.db.model.DepartmentEntity;
import se.sundsvall.snailmail.integration.db.model.RecipientEntity;
import se.sundsvall.snailmail.integration.db.model.RequestEntity;

public final class Mapper {

	private Mapper() {}

	static RequestEntity toRequest(final SendSnailMailRequest request, final RecipientEntity recipientEntity, final DepartmentEntity departmentEntity) {
		return Optional.ofNullable(request)
			.map(req -> {
				final var newRequest = RequestEntity.builder()
					.withDepartmentEntity(departmentEntity)
					.withDeviation(req.getDeviation())
					.withRecipientEntity(recipientEntity)
					.build();

				final var attachments = Optional.ofNullable(req.getAttachments())
					.orElse(List.of()).stream()
					.map(attachment -> toAttachment(attachment, newRequest))
					.toList();

				newRequest.setAttachmentEntities(attachments);

				return newRequest;
			})
			.orElse(null);
	}

	static AttachmentEntity toAttachment(final SendSnailMailRequest.Attachment attachment, final RequestEntity requestEntity) {
		return Optional.ofNullable(attachment)
			.map(attach -> AttachmentEntity.builder()
				.withContent(attach.getContent())
				.withName(attach.getName())
				.withContentType(attach.getContentType())
				.withEnvelopeType(attach.getEnvelopeType())
				.withRequestEntity(requestEntity)
				.build())
			.orElse(null);
	}

	static DepartmentEntity toDepartment(final String departmentName, final BatchEntity batchEntity) {
		return DepartmentEntity.builder()
			.withName(departmentName)
			.withBatchEntity(batchEntity)
			.build();
	}

	static RecipientEntity toRecipient(final SendSnailMailRequest.Address address) {
		return Optional.ofNullable(address).map(notNull -> RecipientEntity.builder()
			.withGivenName(address.getFirstName())
			.withLastName(address.getLastName())
			.withAddress(address.getAddress())
			.withApartmentNumber(address.getApartmentNumber())
			.withPostalCode(address.getZipCode())
			.withCity(address.getCity())
			.withCareOf(address.getCareOf())
			.build())
			.orElse(null);
	}

	static BatchEntity toBatchEntity(final SendSnailMailRequest request) {
		return BatchEntity.builder()
			.withId(request.getBatchId())
			.withIssuer(request.getIssuer())
			.withMunicipalityId(request.getMunicipalityId())
			.build();
	}
}
