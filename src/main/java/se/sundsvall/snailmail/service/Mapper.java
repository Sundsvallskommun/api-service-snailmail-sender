package se.sundsvall.snailmail.service;

import java.util.List;
import java.util.Optional;

import se.sundsvall.snailmail.api.model.SendSnailMailRequest;
import se.sundsvall.snailmail.dto.CitizenDto;
import se.sundsvall.snailmail.dto.SnailMailDto;

import generated.se.sundsvall.citizen.CitizenAddress;
import generated.se.sundsvall.citizen.CitizenExtended;

public final class Mapper {

	private Mapper() {}

	public static SnailMailDto toSnailMailDto(final SendSnailMailRequest request, final CitizenExtended citizen) {
		return SnailMailDto.builder()
				.withCitizenDto(Optional.ofNullable(citizen)
						.map(Mapper::toCitizenDto)
						.orElse(CitizenDto.builder().build()))
				.withDepartment(request.getDepartment())
				.withDeviation(request.getDeviation())
				.withBatchId(request.getBatchId())
				.withAttachments(Optional.ofNullable(request.getAttachments())
						.orElse(List.of()).stream()
						.map(Mapper::toAttachmentDto)
						.toList())
				.build();
	}

	public static SnailMailDto.AttachmentDto toAttachmentDto(final SendSnailMailRequest.Attachment attachment) {
		return SnailMailDto.AttachmentDto.builder()
			.withContent(attachment.getContent())
			.withName(attachment.getName())
			.withContentType(attachment.getContentType())
			.withEnvelopeType(attachment.getEnvelopeType())
			.build();
	}

	private static CitizenDto toCitizenDto(final CitizenExtended citizen) {
		final var address = Optional.ofNullable(citizen.getAddresses()).orElse(List.of()).stream()
			.findFirst()
			.orElse(new CitizenAddress());

		return CitizenDto.builder()
			.withPartyId(citizen.getPersonId().toString())
			.withLastName(citizen.getLastname())
			.withGivenName(citizen.getGivenname())
			.withApartment(address.getAppartmentNumber())
			.withStreet(address.getAddress())
			.withCareOf(address.getCo())
			.withPostalCode(address.getPostalCode())
			.withCity(address.getCity())
			.build();
	}
}
