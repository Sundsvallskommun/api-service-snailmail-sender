package se.sundsvall.snailmail.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import se.sundsvall.dept44.common.validators.annotation.OneOf;
import se.sundsvall.dept44.common.validators.annotation.ValidBase64;
import se.sundsvall.snailmail.api.validation.ValidFolderName;

@Data
@Builder(setterPrefix = "with")
@With
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class SendSnailMailRequest {

	@NotBlank
	@ValidFolderName
	@Schema(description = "DepartmentEntity and unit that should be billed", example = "SBK(Gatuavdelningen, Trafiksektionen)")
	private String department;

	@Schema(description = "If the letter to send deviates from the standard", example = "A3 Ritning")
	private String deviation;

	@NotBlank
	@Schema(description = "BatchEntity id to be used for creating a csv-file", example = "6a5c3d04-412d-11ec-973a-0242ac130043")
	private String batchId;

	@Schema(description = "The issuer of the request", example = "user123")
	private String issuer;

	@Schema(description = "The municipality id", example = "2281")
	private String municipalityId;

	@NotEmpty
	@Schema(description = "Attachments")
	private List<@Valid Attachment> attachments;

	@NotNull
	@Schema(description = "Recipient address")
	private Address address;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor(access = AccessLevel.PACKAGE)
	@Builder(setterPrefix = "with")
	public static class Attachment {

		@ValidBase64
		@Schema(description = "The attachment (file) content as a BASE64-encoded string", example = "aGVsbG8gd29ybGQK")
		private String content;

		@NotBlank
		@Schema(description = "The attachment filename", example = "test.pdf")
		private String name;

		@OneOf("application/pdf")
		@Schema(description = "The attachment content type", example = "application/pdf", allowableValues = {
			"application/pdf"
		})
		private String contentType;

		@Schema(description = "The envelope type for the letter", example = "WINDOWED")
		private EnvelopeType envelopeType;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor(access = AccessLevel.PACKAGE)
	@Builder(setterPrefix = "with")
	public static class Address {

		@Schema(description = "The first name of the recipient", example = "John")
		private String firstName;

		@Schema(description = "The last name of the recipient", example = "Doe")
		private String lastName;

		@Schema(description = "The address", example = "Main Street 1")
		private String address;

		@Schema(description = "The apartment number", example = "1101")
		private String apartmentNumber;

		@Schema(description = "The care of", example = "c/o John Doe")
		private String careOf;

		@Schema(description = "The zip code", example = "12345")
		private String zipCode;

		@Schema(description = "The city", example = "Main Street")
		private String city;

		@Schema(description = "The country", example = "Sweden")
		private String country;

		@Schema(description = "The organization number of the recipient", example = "123456-7890")
		private String organizationNumber;
	}
}
