package se.sundsvall.snailmail.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

@Documented
@Target(TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidAddressValidator.class)
public @interface ValidAddress {

	String message() default "either firstName and lastName, or organizationName must be provided";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
