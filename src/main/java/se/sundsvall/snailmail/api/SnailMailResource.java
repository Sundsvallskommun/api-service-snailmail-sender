package se.sundsvall.snailmail.api;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;
import se.sundsvall.snailmail.api.model.SendSnailMailRequest;
import se.sundsvall.snailmail.api.validation.ValidFolderName;
import se.sundsvall.snailmail.service.SnailMailService;

@RestController
@Validated
@RequestMapping("/{municipalityId}/send")
@Tag(name = "SnailMailSender", description = "SnailMailSender")
@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true)
@ApiResponse(responseCode = "400", description = "Bad RequestEntity", content = @Content(schema = @Schema(oneOf = {
	Problem.class, ConstraintViolationProblem.class
})))
@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = Problem.class)))
class SnailMailResource {

	private final SnailMailService snailMailService;

	SnailMailResource(final SnailMailService snailMailService) {
		this.snailMailService = snailMailService;
	}

	@PostMapping(path = "/snailmail", consumes = APPLICATION_JSON_VALUE)
	@Operation(summary = "Prepare snail mail for batch")
	ResponseEntity<Void> sendSnailMail(
		@Parameter(name = "x-issuer", description = """
			Issuer of the request, is used as a prefix in a folder name.
			Cannot contain any of: '"', '*', '<', '>', '?', '|', '/', '\\', ':'
			""") @RequestHeader(name = "x-issuer", required = false) @ValidFolderName(nullable = true) final String issuer,
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Valid @RequestBody final SendSnailMailRequest request) {

		var decoratedRequest = request
			.withMunicipalityId(municipalityId)
			.withIssuer(issuer);

		snailMailService.sendSnailMail(decoratedRequest);
		return ok().build();
	}

	@PostMapping("batch/{batchId}")
	@Operation(summary = "Send batch")
	ResponseEntity<Void> sendBatch(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "batchId", description = "Batch id", example = "b588d1b7-7911-4e78-89d5-b710b32e0f8e") @ValidUuid @PathVariable final String batchId) {
		snailMailService.sendBatch(municipalityId, batchId);
		return ok().build();
	}

}
