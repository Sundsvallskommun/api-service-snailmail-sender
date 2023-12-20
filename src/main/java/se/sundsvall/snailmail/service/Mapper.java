package se.sundsvall.snailmail.service;

import java.util.List;
import java.util.Optional;

import se.sundsvall.snailmail.api.model.SendSnailMailRequest;
import se.sundsvall.snailmail.dto.CitizenDto;
import se.sundsvall.snailmail.dto.SnailMailDto;

import generated.se.sundsvall.citizen.CitizenAddress;
import generated.se.sundsvall.citizen.CitizenExtended;

public final class Mapper {

	private static final String CURRENT = "Current";

	private Mapper() {}

	public static SnailMailDto toSnailMailDto(SendSnailMailRequest request, CitizenExtended citizen) {

		return SnailMailDto.builder()
			.withCitizenDto(Mapper.toCitizenDto(Optional.ofNullable(citizen).orElse(new CitizenExtended())))
			.withDepartment(request.getDepartment())
			.withDeviation(request.getDeviation())
			.withAttachments(Optional.ofNullable(request.getAttachments()).orElse(List.of()).stream()
				.map(Mapper::toAttachmentDto)
				.toList())
			.build();

	}

	public static SnailMailDto.AttachmentDto toAttachmentDto(SendSnailMailRequest.Attachment attachment) {
		return SnailMailDto.AttachmentDto.builder()
			.withContent(attachment.getContent())
			.withName(attachment.getName())
			.withContentType(attachment.getContentType())
			.withEnvelopeType(attachment.getEnvelopeType())
			.build();
	}

	private static CitizenDto toCitizenDto(CitizenExtended citizen) {

		final var address = Optional.ofNullable(citizen.getAddresses()).orElse(List.of()).stream()
			.filter(address1 -> CURRENT.equals(address1.getStatus()))
			.findFirst()
			.orElse(new CitizenAddress());

		return CitizenDto.builder()
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
