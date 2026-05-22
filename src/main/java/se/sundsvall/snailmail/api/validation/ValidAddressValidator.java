package se.sundsvall.snailmail.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;
import se.sundsvall.snailmail.api.model.SendSnailMailRequest;

public class ValidAddressValidator implements ConstraintValidator<ValidAddress, SendSnailMailRequest.Address> {

	@Override
	public boolean isValid(final SendSnailMailRequest.Address value, final ConstraintValidatorContext context) {
		if (value == null) {
			return true;
		}
		final var hasPersonName = StringUtils.isNotBlank(value.getFirstName()) && StringUtils.isNotBlank(value.getLastName());
		final var hasOrganizationName = StringUtils.isNotBlank(value.getOrganizationName());
		return hasPersonName || hasOrganizationName;
	}
}
