package se.sundsvall.snailmail.dto;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(setterPrefix = "with")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SnailMailDto {

	private CitizenDto citizenDto;
	private List<AttachmentDto> attachments;
	private String department;
	private String deviation;

	@Getter
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	@Builder(setterPrefix = "with")
	public static class AttachmentDto {
		private final String content;
		private final String name;
		private final String contentType;
	}

}
