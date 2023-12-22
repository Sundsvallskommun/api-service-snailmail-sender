package se.sundsvall.snailmail.api.validation;

import static org.springdoc.core.utils.SpringDocUtils.isValidPath;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidFolderNameValidator implements ConstraintValidator<ValidFolderName, String> {

	private static final List<Character> INVALID_FOLDER_CHARS = List.of('"', '*', '<', '>', '?', '|', '/', '\\', ':');

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if(StringUtils.isBlank(value)) {
			return false;
		}

		return INVALID_FOLDER_CHARS.stream()
				.noneMatch(character ->
						value.contains(character.toString()) //Doesn't contain invalid characters
						|| value.endsWith(" ")	//Doesn't end with space
						|| value.endsWith(".")	//Doesn't end with dot
				&& isValidPath(value));
	}
}
