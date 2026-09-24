package com.mechanicmanager.shared.validation;

import com.mechanicmanager.auth.dto.ChangePasswordRequest;
import com.mechanicmanager.auth.dto.LoginRequest;
import com.mechanicmanager.auth.dto.ResetPasswordRequest;
import com.mechanicmanager.users.dto.CreateUserRequest;
import com.mechanicmanager.users.model.Role;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordLengthTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private static String chars(int count) {
        return "a".repeat(count);
    }

    private static Set<String> fields(Set<? extends ConstraintViolation<?>> violations) {
        return violations.stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    @Test
    void acceptsAPasswordOfExactlyTheLimit() {
        assertThat(validator.validate(new ResetPasswordRequest("token", chars(72)))).isEmpty();
    }

    @Test
    void rejectsAPasswordOverTheLimit() {
        var violations = validator.validate(new ResetPasswordRequest("token", chars(73)));

        assertThat(fields(violations)).containsExactly("newPassword");
        assertThat(violations.iterator().next().getMessage()).contains("72");
    }

    @Test
    void measuresBytesNotCharacters() {
        // 40 letras con tilde son 40 caracteres pero 80 bytes en UTF-8.
        String accented = "á".repeat(40);

        assertThat(accented.length()).isEqualTo(40);
        assertThat(fields(validator.validate(new ResetPasswordRequest("token", accented))))
                .containsExactly("newPassword");
    }

    @Test
    void appliesToEveryEntryPointThatHashesOrComparesAPassword() {
        String tooLong = chars(73);

        assertThat(fields(validator.validate(new LoginRequest("ana@example.com", tooLong))))
                .containsExactly("password");
        assertThat(fields(validator.validate(new ChangePasswordRequest(tooLong, tooLong))))
                .containsExactlyInAnyOrder("currentPassword", "newPassword");
        assertThat(fields(validator.validate(
                new CreateUserRequest("Ana", "ana@example.com", tooLong, Role.USER))))
                .containsExactly("password");
    }
}
