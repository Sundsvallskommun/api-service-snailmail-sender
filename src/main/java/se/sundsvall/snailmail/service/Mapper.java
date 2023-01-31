package se.sundsvall.snailmail.service;

import generated.se.sundsvall.citizen.Citizen;
import generated.se.sundsvall.citizen.CitizenAddress;
import org.springframework.stereotype.Component;
import se.sundsvall.snailmail.api.model.SendSnailMailRequest;
import se.sundsvall.snailmail.dto.CitizenDto;
import se.sundsvall.snailmail.dto.SnailMailDto;

import java.util.List;
import java.util.Optional;

@Component

public class Mapper {

    private static final String CURRENT = "Current";

    SnailMailDto toSnailMailDto(SendSnailMailRequest request, Citizen citizen) {

        return SnailMailDto.builder()
                .withCitizenDto(toCitizenDto(Optional.ofNullable(citizen).orElse(new Citizen())))
                .withDepartment(request.getDepartment())
                .withDeviation(request.getDeviation())
                .withAttachments(Optional.ofNullable(request.getAttachments()).orElse(List.of()).stream()
                        .map(this::toAttachmentDto)
                        .toList())
                .build();

    }

    SnailMailDto.AttachmentDto toAttachmentDto(SendSnailMailRequest.Attachment attachment) {
        return SnailMailDto.AttachmentDto.builder()
                .withContent(attachment.getContent())
                .withName(attachment.getName())
                .withContentType(attachment.getContentType())
                .build();
    }

    private CitizenDto toCitizenDto(Citizen citizen) {


        var address = Optional.ofNullable(citizen.getAddresses()).orElse(List.of()).stream()
                .filter(address1 -> address1.getStatus().equals(CURRENT))
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
