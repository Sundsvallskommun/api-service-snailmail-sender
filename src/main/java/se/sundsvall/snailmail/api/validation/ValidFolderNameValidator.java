package se.sundsvall.snailmail.api.validation;

import static org.springdoc.core.utils.SpringDocUtils.isValidPath;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidFolderNameValidator implements ConstraintValidator<ValidFolderName, String> {

	private static final List<Character> INVALID_WINDOWS_SPECIFIC_CHARS = List.of('"', '*', '<', '>', '?', '|', '/', '\\', ':');

	@Override
	public void initialize(ValidFolderName constraintAnnotation) {
		ConstraintValidator.super.initialize(constraintAnnotation);
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if(StringUtils.isBlank(value)) {
			return false;
		}

		return INVALID_WINDOWS_SPECIFIC_CHARS.stream()
				.noneMatch(character -> value.contains(character.toString()))
				&& isValidPath(value);
	}
}
