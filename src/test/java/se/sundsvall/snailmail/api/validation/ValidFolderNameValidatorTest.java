package se.sundsvall.snailmail.api.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ValidFolderNameValidatorTest {

	private final ValidFolderNameValidator validator = new ValidFolderNameValidator();

	@ParameterizedTest
	@ValueSource(strings = {"folder\"", "folder*", "folder<", "folder>", "folder?", "folder|", "folder/", "folder\\", "folder:"})
	void isValid(String folderName) {
		assertThat(validator.isValid(folderName, null)).isFalse();
	}
}