package se.sundsvall.snailmail.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(setterPrefix = "with")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CitizenDto {

	private String partyId;
	private String givenName;
	private String lastName;
	private String street;
	private String apartment;
	private String careOf;
	private String city;
	private String postalCode;
}
