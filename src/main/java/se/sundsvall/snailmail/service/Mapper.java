package se.sundsvall.snailmail.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import se.sundsvall.snailmail.api.model.SendSnailMailRequest;
import se.sundsvall.snailmail.integration.db.model.AttachmentEntity;
import se.sundsvall.snailmail.integration.db.model.BatchEntity;
import se.sundsvall.snailmail.integration.db.model.DepartmentEntity;
import se.sundsvall.snailmail.integration.db.model.RecipientEntity;
import se.sundsvall.snailmail.integration.db.model.RequestEntity;

import generated.se.sundsvall.citizen.CitizenExtended;

public final class Mapper {

	private Mapper() {}

	static RequestEntity toRequest(final SendSnailMailRequest request, final CitizenExtended citizen, final DepartmentEntity departmentEntity) {

		return Optional.ofNullable(request)
			.map(req -> {
				final var newRequest = RequestEntity.builder()
					.withDepartmentEntity(departmentEntity)
					.withDeviation(req.getDeviation())
					.withRecipientEntity(toRecipient(citizen))
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

	static RecipientEntity toRecipient(final CitizenExtended citizen) {

		return Optional.ofNullable(citizen)
			.flatMap(c -> Optional.ofNullable(c.getAddresses())
				.orElse(Collections.emptyList())
				.stream()
				.findFirst())
			.map(address -> RecipientEntity.builder()
				.withGivenName(citizen.getGivenname())
				.withLastName(citizen.getLastname())
				.withAddress(address.getAddress())
				.withApartmentNumber(address.getAppartmentNumber())
				.withPostalCode(address.getPostalCode())
				.withCity(address.getCity())
				.withCareOf(address.getCo())
				.build())
			.orElse(null);
	}

}
