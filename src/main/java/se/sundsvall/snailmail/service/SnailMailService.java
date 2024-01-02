package se.sundsvall.snailmail.service;

import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;
import static se.sundsvall.snailmail.service.Mapper.toSnailMailDto;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;

import se.sundsvall.dept44.util.jacoco.ExcludeFromJacocoGeneratedCoverageReport;
import se.sundsvall.snailmail.api.model.SendSnailMailRequest;
import se.sundsvall.snailmail.integration.citizen.CitizenIntegration;
import se.sundsvall.snailmail.integration.samba.SambaIntegration;

@Service
public class SnailMailService {

	private final SambaIntegration sambaIntegration;
	private final CitizenIntegration citizenIntegration;

	public SnailMailService(final SambaIntegration sambaIntegration, final CitizenIntegration citizenIntegration) {
		this.sambaIntegration = sambaIntegration;
		this.citizenIntegration = citizenIntegration;
	}

	public void sendSnailMail(final SendSnailMailRequest request) {
		final var citizenExtended = citizenIntegration.getCitizen(request.getPartyId());

		final var snailMailDto = toSnailMailDto(request, citizenExtended);

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

	@ExcludeFromJacocoGeneratedCoverageReport
	public void sendBatch(final String batchId) {
		//Should create CSVs for batch and put them in the samba share
		// Implemented in different story
	}

}
