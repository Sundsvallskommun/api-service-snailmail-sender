package se.sundsvall.snailmail.api.validation;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target({
	FIELD, CONSTRUCTOR, PARAMETER
})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidFolderNameValidator.class)
public @interface ValidFolderName {

	String message() default "not a valid folder name";

	boolean nullable() default false;

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
