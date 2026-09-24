package com.mechanicmanager.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.nio.charset.StandardCharsets;

public class PasswordLengthValidator implements ConstraintValidator<PasswordLength, String> {

    static final int MAX_BYTES = 72;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || value.getBytes(StandardCharsets.UTF_8).length <= MAX_BYTES;
    }
}
