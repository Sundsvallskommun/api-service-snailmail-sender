package se.sundsvall.snailmail.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.snailmail.TestDataFactory.buildCitizenExtended;
import static se.sundsvall.snailmail.TestDataFactory.buildSendSnailMailRequest;
import static se.sundsvall.snailmail.service.SnailMailService.ADDRESS;
import static se.sundsvall.snailmail.service.SnailMailService.CITY;
import static se.sundsvall.snailmail.service.SnailMailService.GIVEN_NAME;
import static se.sundsvall.snailmail.service.SnailMailService.LAST_NAME;
import static se.sundsvall.snailmail.service.SnailMailService.POSTAL_CODE;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.snailmail.integration.citizen.CitizenIntegration;
import se.sundsvall.snailmail.integration.db.BatchRepository;
import se.sundsvall.snailmail.integration.db.DepartmentRepository;
import se.sundsvall.snailmail.integration.db.RequestRepository;
import se.sundsvall.snailmail.integration.db.model.BatchEntity;
import se.sundsvall.snailmail.integration.db.model.DepartmentEntity;
import se.sundsvall.snailmail.integration.db.model.RequestEntity;
import se.sundsvall.snailmail.integration.samba.SambaIntegration;

import generated.se.sundsvall.citizen.CitizenExtended;

@ExtendWith(MockitoExtension.class)
class SnailMailServiceTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String X_ISSUER = "issuer";
	private static final String BATCH_ID = "batchId";

	@Mock
	private CitizenIntegration citizenIntegrationMock;

	@Mock
	private BatchRepository batchRepositoryMock;

	@Mock
	private DepartmentRepository departmentRepositoryMock;

	@Mock
	private RequestRepository requestRepositoryMock;

	@Mock
	private SambaIntegration sambaIntegrationMock;

	@InjectMocks
	private SnailMailService snailMailService;

	@Test
	void sendMail() {
		//Arrange
		final var batchEntity = BatchEntity.builder().build();
		final var request = buildSendSnailMailRequest();
		when(batchRepositoryMock.findByMunicipalityIdAndId(MUNICIPALITY_ID, request.getBatchId())).thenReturn(Optional.ofNullable(batchEntity));
		when(departmentRepositoryMock.findByNameAndBatchEntity(any(String.class), any(BatchEntity.class))).thenReturn(Optional.ofNullable(DepartmentEntity.builder().build()));
		when(citizenIntegrationMock.getCitizen(any(String.class))).thenReturn(buildCitizenExtended());

		//Act
		snailMailService.sendSnailMail(MUNICIPALITY_ID, request, X_ISSUER);

		//Assert
		verify(batchRepositoryMock).findByMunicipalityIdAndId(MUNICIPALITY_ID, request.getBatchId());
		verify(departmentRepositoryMock).findByNameAndBatchEntity("someDepartment", batchEntity);
		verify(requestRepositoryMock).save(any(RequestEntity.class));
		verify(citizenIntegrationMock).getCitizen(any(String.class));
		verifyNoMoreInteractions(batchRepositoryMock, departmentRepositoryMock, requestRepositoryMock, citizenIntegrationMock);
		verifyNoInteractions(sambaIntegrationMock);

	}

	@Test
	void sendMailWithNewBatch() {
		//Arrange
		final var batchEntity = BatchEntity.builder().build();
		final var departmentEntity = DepartmentEntity.builder().build();
		final var request = buildSendSnailMailRequest();

		when(citizenIntegrationMock.getCitizen(any(String.class))).thenReturn(buildCitizenExtended());
		when(batchRepositoryMock.findByMunicipalityIdAndId(MUNICIPALITY_ID, request.getBatchId())).thenReturn(Optional.empty());
		when(batchRepositoryMock.save(any(BatchEntity.class))).thenReturn(batchEntity);

		when(departmentRepositoryMock.findByNameAndBatchEntity(anyString(), any(BatchEntity.class))).thenReturn(Optional.empty());
		when(departmentRepositoryMock.save(any(DepartmentEntity.class))).thenReturn(departmentEntity);

		when(requestRepositoryMock.save(any(RequestEntity.class))).thenReturn(RequestEntity.builder().build());

		//Act
		snailMailService.sendSnailMail(MUNICIPALITY_ID, request, X_ISSUER);

		//Assert
		verify(citizenIntegrationMock).getCitizen(any(String.class));
		verify(batchRepositoryMock).findByMunicipalityIdAndId(MUNICIPALITY_ID, request.getBatchId());
		verify(batchRepositoryMock).save(any(BatchEntity.class));

		verify(departmentRepositoryMock).findByNameAndBatchEntity("someDepartment", batchEntity);
		verify(departmentRepositoryMock).save(any(DepartmentEntity.class));

		verify(requestRepositoryMock).save(any(RequestEntity.class));
		verifyNoMoreInteractions(batchRepositoryMock, departmentRepositoryMock, requestRepositoryMock, citizenIntegrationMock);
		verifyNoInteractions(sambaIntegrationMock);

	}

	@Test
	void sendMailWithNewDepartment() {
		//Arrange
		final var batchEntity = BatchEntity.builder().build();
		final var request = buildSendSnailMailRequest();

		when(citizenIntegrationMock.getCitizen(any(String.class))).thenReturn(buildCitizenExtended());
		when(batchRepositoryMock.findByMunicipalityIdAndId(MUNICIPALITY_ID, request.getBatchId())).thenReturn(Optional.ofNullable(batchEntity));
		when(departmentRepositoryMock.findByNameAndBatchEntity(any(String.class), any(BatchEntity.class))).thenReturn(Optional.empty());
		when(departmentRepositoryMock.save(any(DepartmentEntity.class))).thenReturn(DepartmentEntity.builder().build());
		when(requestRepositoryMock.save(any(RequestEntity.class))).thenReturn(RequestEntity.builder().build());

		//Act
		snailMailService.sendSnailMail(MUNICIPALITY_ID, request, X_ISSUER);

		//Assert
		verify(citizenIntegrationMock).getCitizen(any(String.class));
		verify(batchRepositoryMock).findByMunicipalityIdAndId(MUNICIPALITY_ID, request.getBatchId());
		verify(departmentRepositoryMock).findByNameAndBatchEntity(anyString(), any(BatchEntity.class));
		verify(departmentRepositoryMock).save(any(DepartmentEntity.class));
		verify(requestRepositoryMock).save(any(RequestEntity.class));
		verifyNoMoreInteractions(batchRepositoryMock, departmentRepositoryMock, requestRepositoryMock, citizenIntegrationMock);
		verifyNoInteractions(sambaIntegrationMock);

	}

	@Test
	void sendMailWithNewBatchAndDepartment() {
		// Arrange
		final var request = buildSendSnailMailRequest();

		when(citizenIntegrationMock.getCitizen(any(String.class))).thenReturn(buildCitizenExtended());
		when(batchRepositoryMock.findByMunicipalityIdAndId(MUNICIPALITY_ID, request.getBatchId())).thenReturn(Optional.empty());
		when(batchRepositoryMock.save(any(BatchEntity.class))).thenReturn(BatchEntity.builder().build());
		when(departmentRepositoryMock.findByNameAndBatchEntity(any(String.class), any(BatchEntity.class))).thenReturn(Optional.empty());
		when(departmentRepositoryMock.save(any(DepartmentEntity.class))).thenReturn(DepartmentEntity.builder().build());

		// Act
		snailMailService.sendSnailMail(MUNICIPALITY_ID, request, X_ISSUER);

		// Assert
		verify(batchRepositoryMock).findByMunicipalityIdAndId(MUNICIPALITY_ID, request.getBatchId());
		verify(batchRepositoryMock).save(any(BatchEntity.class));
		verify(departmentRepositoryMock).findByNameAndBatchEntity(anyString(), any(BatchEntity.class));
		verify(departmentRepositoryMock).save(any(DepartmentEntity.class));
		verify(requestRepositoryMock).save(any(RequestEntity.class));
		verify(citizenIntegrationMock).getCitizen(any(String.class));
		verifyNoMoreInteractions(batchRepositoryMock, departmentRepositoryMock, requestRepositoryMock, citizenIntegrationMock);
		verifyNoInteractions(sambaIntegrationMock);

	}

	@Test
	void sendMailWithNewBatchAndDepartmentAndCitizen() {
		// Arrange
		final var request = buildSendSnailMailRequest();
		when(citizenIntegrationMock.getCitizen(any(String.class))).thenReturn(buildCitizenExtended());
		when(batchRepositoryMock.findByMunicipalityIdAndId(MUNICIPALITY_ID, request.getBatchId())).thenReturn(Optional.empty());
		when(batchRepositoryMock.save(any(BatchEntity.class))).thenReturn(BatchEntity.builder().build());
		when(departmentRepositoryMock.findByNameAndBatchEntity(any(String.class), any(BatchEntity.class))).thenReturn(Optional.empty());
		when(departmentRepositoryMock.save(any(DepartmentEntity.class))).thenReturn(DepartmentEntity.builder().build());

		// Act
		snailMailService.sendSnailMail(MUNICIPALITY_ID, request, X_ISSUER);

		// Assert
		verify(batchRepositoryMock).findByMunicipalityIdAndId(MUNICIPALITY_ID, request.getBatchId());
		verify(batchRepositoryMock).save(any(BatchEntity.class));
		verify(departmentRepositoryMock).findByNameAndBatchEntity(any(String.class), any(BatchEntity.class));
		verify(departmentRepositoryMock).save(any(DepartmentEntity.class));
		verify(requestRepositoryMock).save(any(RequestEntity.class));
		verify(citizenIntegrationMock).getCitizen(any(String.class));
		verifyNoMoreInteractions(batchRepositoryMock, departmentRepositoryMock, requestRepositoryMock, citizenIntegrationMock);
		verifyNoInteractions(sambaIntegrationMock);

	}

	@Test
	void sendBatch() {
		// Arrange
		final var batchEntity = BatchEntity.builder().build();

		when(batchRepositoryMock.findByMunicipalityIdAndId(MUNICIPALITY_ID, BATCH_ID)).thenReturn(Optional.ofNullable(batchEntity));

		// Act
		snailMailService.sendBatch(MUNICIPALITY_ID, BATCH_ID);

		// Assert
		verify(batchRepositoryMock).findByMunicipalityIdAndId(MUNICIPALITY_ID, BATCH_ID);
		verify(sambaIntegrationMock).writeBatchDataToSambaShare(any(BatchEntity.class));
		verify(batchRepositoryMock).delete(any(BatchEntity.class));
		verifyNoMoreInteractions(batchRepositoryMock, sambaIntegrationMock);
		verifyNoInteractions(departmentRepositoryMock, requestRepositoryMock, citizenIntegrationMock);
	}

	@Test
	void sendBatchWithNoBatchFound() {
		// Arrange
		when(batchRepositoryMock.findByMunicipalityIdAndId(MUNICIPALITY_ID, BATCH_ID)).thenReturn(Optional.empty());

		// Act
		assertThatThrownBy(() -> snailMailService.sendBatch(MUNICIPALITY_ID, BATCH_ID))
			.isInstanceOf(Problem.class)
			.hasMessage("No batch found: Failed to fetch batch data from database");

		// Assert
		verify(batchRepositoryMock).findByMunicipalityIdAndId(MUNICIPALITY_ID, BATCH_ID);
		verifyNoMoreInteractions(batchRepositoryMock);
		verifyNoInteractions(departmentRepositoryMock, sambaIntegrationMock, requestRepositoryMock, citizenIntegrationMock);
	}

	@Test
	void sendBatchWithoutEvelopeType() {
		// Arrange
		final var batchEntity = BatchEntity.builder().build();
		final var request = buildSendSnailMailRequest();
		request.getAttachments().getFirst().setEnvelopeType(null);

		// Mock
		when(batchRepositoryMock.findByMunicipalityIdAndId(MUNICIPALITY_ID, request.getBatchId())).thenReturn(Optional.ofNullable(batchEntity));
		when(departmentRepositoryMock.findByNameAndBatchEntity(any(String.class), any(BatchEntity.class))).thenReturn(Optional.ofNullable(DepartmentEntity.builder().build()));
		when(citizenIntegrationMock.getCitizen(any(String.class))).thenReturn(buildCitizenExtended());

		// Act
		snailMailService.sendSnailMail(MUNICIPALITY_ID, request, X_ISSUER);

		// Assert
		verify(batchRepositoryMock).findByMunicipalityIdAndId(MUNICIPALITY_ID, request.getBatchId());
		verify(departmentRepositoryMock).findByNameAndBatchEntity(any(String.class), any(BatchEntity.class));
		verify(requestRepositoryMock).save(any());
		verify(citizenIntegrationMock).getCitizen(any(String.class));
		verifyNoMoreInteractions(batchRepositoryMock, departmentRepositoryMock, requestRepositoryMock, citizenIntegrationMock);
		verifyNoInteractions(sambaIntegrationMock);
	}

	@ParameterizedTest
	@MethodSource("provideIncompleteAddress")
	void sendBatchWithIncompleteAddress_shouldThrowException(CitizenExtended citizenExtended, String missingField) {
		// Arrange
		final var snailMailRequest = buildSendSnailMailRequest();
		when(citizenIntegrationMock.getCitizen(snailMailRequest.getPartyId())).thenReturn(citizenExtended);

		// Act & Assert
		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> snailMailService.sendSnailMail(MUNICIPALITY_ID, snailMailRequest, X_ISSUER))
			.satisfies(throwableProblem -> {
				assertThat(throwableProblem.getStatus()).isEqualTo(Status.INTERNAL_SERVER_ERROR);
				assertThat(throwableProblem.getTitle()).isEqualTo("Citizen with partyId: " + citizenExtended.getPersonId() + " is missing required address information");
				assertThat(throwableProblem.getDetail()).contains("Missing fields: " + missingField);
			});

		verify(citizenIntegrationMock).getCitizen(snailMailRequest.getPartyId());
		verifyNoMoreInteractions(citizenIntegrationMock);
		verifyNoInteractions(batchRepositoryMock, departmentRepositoryMock, requestRepositoryMock, sambaIntegrationMock);
	}

	@Test
	void sendBatchWithAllRequiredAddressFieldsMissing_shouldThrowException() {
		// Arrange
		final var snailMailRequest = buildSendSnailMailRequest();
		final var citizenExtended = buildCitizenExtended();
		citizenExtended.setGivenname(null);
		citizenExtended.setLastname(null);
		citizenExtended.getAddresses().getFirst().setAddress(null);
		citizenExtended.getAddresses().getFirst().setPostalCode(null);
		citizenExtended.getAddresses().getFirst().setCity(null);

		when(citizenIntegrationMock.getCitizen(snailMailRequest.getPartyId())).thenReturn(citizenExtended);

		// Act & Assert
		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> snailMailService.sendSnailMail(MUNICIPALITY_ID, snailMailRequest,X_ISSUER))
			.satisfies(throwableProblem -> {
				assertThat(throwableProblem.getStatus()).isEqualTo(Status.INTERNAL_SERVER_ERROR);
				assertThat(throwableProblem.getTitle()).isEqualTo("Citizen with partyId: " + citizenExtended.getPersonId() + " is missing required address information");
				assertThat(throwableProblem.getDetail()).contains("Missing fields: " + GIVEN_NAME + ", " + LAST_NAME + ", " + ADDRESS + ", " + POSTAL_CODE + ", " + CITY);
			});
	}

	@Test
	void sendBatchWithMissingAddress_shouldThrowException() {
		// Arrange
		final var snailMailRequest = buildSendSnailMailRequest();
		final var citizenExtended = buildCitizenExtended();
		citizenExtended.setAddresses(null);

		when(citizenIntegrationMock.getCitizen(snailMailRequest.getPartyId())).thenReturn(citizenExtended);

		// Act & Assert
		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> snailMailService.sendSnailMail(MUNICIPALITY_ID, snailMailRequest, X_ISSUER))
			.satisfies(throwableProblem -> {
				assertThat(throwableProblem.getStatus()).isEqualTo(Status.INTERNAL_SERVER_ERROR);
				assertThat(throwableProblem.getTitle()).isEqualTo("No address information found for citizen");
				assertThat(throwableProblem.getDetail()).isEqualTo("No address information found for citizen with partyId: " + citizenExtended.getPersonId());
			});
	}

	private static Stream<Arguments> provideIncompleteAddress() {
		final var missingGivenName = buildCitizenExtended();
		missingGivenName.setGivenname(null);

		final var missingLastName = buildCitizenExtended();
		missingLastName.setLastname(null);

		final var missingAddress = buildCitizenExtended();
		missingAddress.getAddresses().getFirst().setAddress(null);

		final var missingPostalCode = buildCitizenExtended();
		missingPostalCode.getAddresses().getFirst().setPostalCode("");

		final var missingCity = buildCitizenExtended();
		missingCity.getAddresses().getFirst().setCity(" ");

		return Stream.of(
			Arguments.of(missingGivenName, GIVEN_NAME),
			Arguments.of(missingLastName, LAST_NAME),
			Arguments.of(missingAddress, ADDRESS),
			Arguments.of(missingPostalCode, POSTAL_CODE),
			Arguments.of(missingCity, CITY)
		);
	}
}
