package com.mechanicmanager.auth.service;

import com.mechanicmanager.auth.dto.LoginRequest;
import com.mechanicmanager.auth.dto.LoginResponse;
import com.mechanicmanager.auth.exception.InvalidCredentialsException;
import com.mechanicmanager.auth.exception.TooManyAttemptsException;
import com.mechanicmanager.shared.security.AttemptLimiter;
import com.mechanicmanager.shared.security.AuthenticatedUser;
import com.mechanicmanager.shared.security.JwtService;
import com.mechanicmanager.users.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String IP = "203.0.113.9";

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    private AttemptLimiter limiter;
    private AuthService service;

    @BeforeEach
    void setUp() {
        limiter = new AttemptLimiter(Clock.systemUTC(), 15);
        service = new AuthService(authenticationManager, jwtService, limiter);
    }

    private LoginRequest request(String email, String password) {
        return new LoginRequest(email, password);
    }

    private void wrongPassword() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("mala"));
    }

    @Test
    void blocksAnEmailAfterTooManyFailures() {
        wrongPassword();

        for (int i = 0; i < AuthService.MAX_FAILURES_PER_EMAIL; i++) {
            assertThatThrownBy(() -> service.login(request("ana@example.com", "x"), IP))
                    .isInstanceOf(InvalidCredentialsException.class);
        }

        assertThatThrownBy(() -> service.login(request("ana@example.com", "x"), IP))
                .isInstanceOf(TooManyAttemptsException.class);
    }

    @Test
    void doesNotEvenCheckThePasswordOnceBlocked() {
        wrongPassword();
        for (int i = 0; i < AuthService.MAX_FAILURES_PER_EMAIL; i++) {
            assertThatThrownBy(() -> service.login(request("ana@example.com", "x"), IP))
                    .isInstanceOf(InvalidCredentialsException.class);
        }

        assertThatThrownBy(() -> service.login(request("ana@example.com", "correcta"), IP))
                .isInstanceOf(TooManyAttemptsException.class);

        verify(authenticationManager, org.mockito.Mockito.times(AuthService.MAX_FAILURES_PER_EMAIL))
                .authenticate(any());
    }

    @Test
    void normalizesTheEmailSoCaseDoesNotEvadeTheLimit() {
        wrongPassword();

        for (int i = 0; i < AuthService.MAX_FAILURES_PER_EMAIL; i++) {
            assertThatThrownBy(() -> service.login(request("  ANA@Example.com ", "x"), IP))
                    .isInstanceOf(InvalidCredentialsException.class);
        }

        assertThatThrownBy(() -> service.login(request("ana@example.com", "x"), IP))
                .isInstanceOf(TooManyAttemptsException.class);
    }

    @Test
    void blocksAnIpAfterTooManyFailuresAcrossEmails() {
        wrongPassword();

        for (int i = 0; i < AuthService.MAX_FAILURES_PER_IP; i++) {
            String email = "user" + i + "@example.com";
            assertThatThrownBy(() -> service.login(request(email, "x"), IP))
                    .isInstanceOf(InvalidCredentialsException.class);
        }

        assertThatThrownBy(() -> service.login(request("otro@example.com", "x"), IP))
                .isInstanceOf(TooManyAttemptsException.class);
        assertThat(limiter.isBlocked("login:ip:198.51.100.1", 1)).isFalse();
    }

    @Test
    void successfulLoginClearsTheEmailFailures() {
        AuthenticatedUser user = new AuthenticatedUser(
                7L, "Ana", "ana@example.com", "hash", Role.USER, true, false);
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("mala"))
                .thenThrow(new BadCredentialsException("mala"))
                .thenReturn(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        when(jwtService.generateToken(user)).thenReturn("jwt");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        for (int i = 0; i < 2; i++) {
            assertThatThrownBy(() -> service.login(request("ana@example.com", "x"), IP))
                    .isInstanceOf(InvalidCredentialsException.class);
        }
        LoginResponse response = service.login(request("ana@example.com", "correcta"), IP);

        assertThat(response.token()).isEqualTo("jwt");
        assertThat(limiter.isBlocked("login:email:ana@example.com", 1)).isFalse();
    }

    @Test
    void neverCallsTheAuthenticationManagerWhenAlreadyBlockedByIp() {
        for (int i = 0; i < AuthService.MAX_FAILURES_PER_IP; i++) {
            limiter.record("login:ip:" + IP);
        }

        assertThatThrownBy(() -> service.login(request("ana@example.com", "x"), IP))
                .isInstanceOf(TooManyAttemptsException.class);

        verify(authenticationManager, never()).authenticate(any());
    }
}
