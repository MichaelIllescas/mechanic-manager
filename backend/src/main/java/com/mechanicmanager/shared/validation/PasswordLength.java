package com.mechanicmanager.shared.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Una contraseña no puede superar los 72 bytes en UTF-8: es el límite de BCrypt,
 * y pasado ese punto el encoder lanza una excepción en vez de truncar.
 *
 * <p>Se mide en bytes y no en caracteres porque una letra con tilde o un emoji
 * ocupan más de un byte. Un valor nulo se considera válido: la obligatoriedad
 * la decide {@code @NotBlank}.
 */
@Documented
@Constraint(validatedBy = PasswordLengthValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordLength {

    String message() default "La contraseña es demasiado larga (máximo 72 caracteres)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
