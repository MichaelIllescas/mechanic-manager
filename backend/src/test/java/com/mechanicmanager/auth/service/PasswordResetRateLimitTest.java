package com.mechanicmanager.auth.service;

import com.mechanicmanager.auth.dto.ForgotPasswordRequest;
import com.mechanicmanager.auth.dto.ResetPasswordRequest;
import com.mechanicmanager.auth.exception.InvalidPasswordResetTokenException;
import com.mechanicmanager.auth.exception.TooManyAttemptsException;
import com.mechanicmanager.auth.repository.PasswordResetTokenRepository;
import com.mechanicmanager.shared.email.EmailSender;
import com.mechanicmanager.shared.security.AttemptLimiter;
import com.mechanicmanager.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetRateLimitTest {

    private static final String IP = "203.0.113.9";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private EmailSender emailSender;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AttemptLimiter limiter;

    @BeforeEach
    void setUp() {
        limiter = new AttemptLimiter(Clock.systemUTC(), 15);
    }

    private RequestPasswordResetUseCase forgot() {
        return new RequestPasswordResetUseCase(
                userRepository, tokenRepository, emailSender, limiter,
                30, "http://localhost:4200", "/reset-password");
    }

    @Test
    void limitsForgotPasswordRequestsPerEmailEvenWhenTheAccountDoesNotExist() {
        RequestPasswordResetUseCase useCase = forgot();
        ForgotPasswordRequest request = new ForgotPasswordRequest("nadie@example.com");
        when(userRepository.findByEmail("nadie@example.com")).thenReturn(Optional.empty());

        for (int i = 0; i < RequestPasswordResetUseCase.MAX_REQUESTS_PER_EMAIL; i++) {
            assertThatCode(() -> useCase.execute(request, IP)).doesNotThrowAnyException();
        }

        assertThatThrownBy(() -> useCase.execute(request, IP))
                .isInstanceOf(TooManyAttemptsException.class);
    }

    @Test
    void limitsForgotPasswordRequestsPerIp() {
        RequestPasswordResetUseCase useCase = forgot();
        when(userRepository.findByEmail(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(Optional.empty());

        for (int i = 0; i < RequestPasswordResetUseCase.MAX_REQUESTS_PER_IP; i++) {
            String email = "user" + i + "@example.com";
            assertThatCode(() -> useCase.execute(new ForgotPasswordRequest(email), IP))
                    .doesNotThrowAnyException();
        }

        assertThatThrownBy(() -> useCase.execute(new ForgotPasswordRequest("otro@example.com"), IP))
                .isInstanceOf(TooManyAttemptsException.class);
    }

    @Test
    void limitsResetAttemptsWithInvalidTokensPerIp() {
        ResetPasswordUseCase useCase =
                new ResetPasswordUseCase(tokenRepository, userRepository, passwordEncoder, limiter);
        ResetPasswordRequest request = new ResetPasswordRequest("token-inventado", "definitiva-456");
        when(tokenRepository.findByTokenHash(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(Optional.empty());

        for (int i = 0; i < ResetPasswordUseCase.MAX_FAILURES_PER_IP; i++) {
            assertThatThrownBy(() -> useCase.execute(request, IP))
                    .isInstanceOf(InvalidPasswordResetTokenException.class);
        }

        assertThatThrownBy(() -> useCase.execute(request, IP))
                .isInstanceOf(TooManyAttemptsException.class);
    }
}
