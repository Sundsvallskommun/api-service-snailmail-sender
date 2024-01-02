package se.sundsvall.snailmail.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import se.sundsvall.snailmail.api.model.SendSnailMailRequest;
import se.sundsvall.snailmail.integration.db.model.Attachment;
import se.sundsvall.snailmail.integration.db.model.Batch;
import se.sundsvall.snailmail.integration.db.model.Department;
import se.sundsvall.snailmail.integration.db.model.Recipient;
import se.sundsvall.snailmail.integration.db.model.Request;

import generated.se.sundsvall.citizen.CitizenExtended;

public final class Mapper {

	private Mapper() {}

	static Request toRequest(final SendSnailMailRequest request, final CitizenExtended citizen, final Department department) {

		return Optional.ofNullable(request)
			.map(req -> Request.builder()
				.withDepartment(department)
				.withDeviation(req.getDeviation())
				.withRecipient(toRecipient(citizen))
				.withAttachments(Optional.ofNullable(req.getAttachments())
					.orElse(List.of()).stream()
					.map(Mapper::toAttachment)
					.toList())
				.build())
			.orElse(null);

	}

	static Attachment toAttachment(final SendSnailMailRequest.Attachment attachment) {

		return Optional.ofNullable(attachment)
			.map(attach -> Attachment.builder()
				.withContent(attach.getContent())
				.withName(attach.getName())
				.withContentType(attach.getContentType())
				.withEnvelopeType(attach.getEnvelopeType())
				.build())
			.orElse(null);

	}

	static Department toDepartment(final String departmentName, final Batch batch) {

		return Department.builder()
			.withName(departmentName)
			.withBatch(batch)
			.build();
	}

	static Recipient toRecipient(final CitizenExtended citizen) {

		return Optional.ofNullable(citizen)
			.flatMap(c -> Optional.ofNullable(c.getAddresses())
				.orElse(Collections.emptyList())
				.stream()
				.findFirst())
			.map(address -> Recipient.builder()
				.withGivenName(citizen.getGivenname())
				.withLastName(citizen.getLastname())
				.withAdress(address.getAddress())
				.withPostalCode(address.getPostalCode())
				.withCity(address.getCity())
				.withCo(address.getCo())
				.build())
			.orElse(null);
	}

}
