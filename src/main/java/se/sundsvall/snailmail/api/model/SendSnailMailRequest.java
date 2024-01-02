package se.sundsvall.snailmail.api.model;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import se.sundsvall.dept44.common.validators.annotation.OneOf;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;
import se.sundsvall.snailmail.api.validation.ValidFolderName;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class SendSnailMailRequest {

	@NotBlank
	@ValidFolderName
	@Schema(description = "Department and unit that should be billed", example = "SBK(Gatuavdelningen, Trafiksektionen)")
	private String department;

	@Schema(description = "If the letter to send deviates from the standard", example = "A3 Ritning")
	private String deviation;

	@NotBlank
	@Schema(description = "Batch id to be used for creating a csv-file", example = "6a5c3d04-412d-11ec-973a-0242ac130043")
	private String batchId;

	@Schema(description = "Party id for the person the letter should be sent to", example = "6a5c3d04-412d-11ec-973a-0242ac130003")
	@ValidUuid
	private String partyId;

	@Schema(description = "Attachments")
	private List<@Valid Attachment> attachments;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor(access = AccessLevel.PACKAGE)
	@Builder(setterPrefix = "with")
	public static class Attachment {

		@Schema(description = "The attachment (file) content as a BASE64-encoded string", example = "aGVsbG8gd29ybGQK")
		@NotBlank
		private String content;

		@Schema(description = "The attachment filename", example = "test.txt")
		@NotBlank
		private String name;

		@OneOf("application/pdf")
		@Schema(description = "The attachment content type", example = "application/pdf", allowableValues = {"application/pdf"})
		private String contentType;

		@Schema(description = "The envelope type for the letter", example = "WINDOWED")
		private EnvelopeType envelopeType;

	}

}
