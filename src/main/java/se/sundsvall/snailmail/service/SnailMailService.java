package se.sundsvall.snailmail.service;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;
import static se.sundsvall.snailmail.service.Mapper.toSnailMailDto;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;

import se.sundsvall.snailmail.api.model.SendSnailMailRequest;
import se.sundsvall.snailmail.dto.CitizenDto;
import se.sundsvall.snailmail.integration.citizen.CitizenIntegration;
import se.sundsvall.snailmail.integration.emailsender.EmailSenderIntegration;
import se.sundsvall.snailmail.integration.samba.SambaIntegration;

@Service
public class SnailMailService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SnailMailService.class);

	private final SambaIntegration sambaIntegration;
	private final CitizenIntegration citizenIntegration;
	private final EmailSenderIntegration emailSenderIntegration;

	public SnailMailService(SambaIntegration sambaIntegration, CitizenIntegration citizenIntegration, EmailSenderIntegration emailSenderIntegration) {
		this.sambaIntegration = sambaIntegration;
		this.citizenIntegration = citizenIntegration;
		this.emailSenderIntegration = emailSenderIntegration;
	}

	public void saveSnailMailForBatch(SendSnailMailRequest request) {
		//Fetch citizen data for one citizen, using batch instead of a new integration.
		var citizenExtended = citizenIntegration.getCitizens(List.of(request.getPartyId())).getFirst();

		var snailMailDto = toSnailMailDto(request, citizenExtended);

		checkForEmptyCitizenData(snailMailDto.getCitizenDto());

		try {
			sambaIntegration.writeBatchDataToSambaShare(snailMailDto);
		} catch (IOException e) {
			throw Problem.builder()
					.withTitle("Failed when writing files to samba share")
					.withStatus(INTERNAL_SERVER_ERROR)
					.withDetail(e.getMessage())
					.build();
		}
	}

	public void checkForEmptyCitizenData(CitizenDto citizenDto) {
		//If any of these are empty there's no point in sending the snailmail.
		if (citizenDto == null || isBlank(citizenDto.getGivenName()) || isBlank(citizenDto.getLastName())) {
			throw Problem.builder()
					.withTitle("Citizen data is missing, cannot deliver as snailmail")
					.withStatus(INTERNAL_SERVER_ERROR)
					.withDetail("Missing data from Citizen API")
					.build();
		}
	}
}
