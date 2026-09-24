package com.mechanicmanager.auth.service;

import com.mechanicmanager.auth.dto.LoginRequest;
import com.mechanicmanager.auth.dto.LoginResponse;
import com.mechanicmanager.auth.exception.AccountDisabledException;
import com.mechanicmanager.auth.exception.InvalidCredentialsException;
import com.mechanicmanager.auth.exception.TooManyAttemptsException;
import com.mechanicmanager.shared.exception.AppException;
import com.mechanicmanager.shared.security.AttemptLimiter;
import com.mechanicmanager.shared.security.AuthenticatedUser;
import com.mechanicmanager.shared.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String TOKEN_TYPE = "Bearer";

    /**
     * Por email es el límite que frena la fuerza bruta contra una cuenta. Por IP
     * es más holgado a propósito: detrás de un proxy o de una red compartida
     * muchas personas comparten dirección.
     */
    static final int MAX_FAILURES_PER_EMAIL = 5;
    static final int MAX_FAILURES_PER_IP = 20;

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AttemptLimiter attemptLimiter;

    public LoginResponse login(LoginRequest request, String clientIp) {
        String email = request.email().trim().toLowerCase();
        String emailKey = "login:email:" + email;
        String ipKey = "login:ip:" + clientIp;

        if (attemptLimiter.isBlocked(emailKey, MAX_FAILURES_PER_EMAIL)
                || attemptLimiter.isBlocked(ipKey, MAX_FAILURES_PER_IP)) {
            throw new TooManyAttemptsException();
        }

        Authentication authentication;
        try {
            authentication = authenticate(email, request.password());
        } catch (AppException e) {
            attemptLimiter.record(emailKey);
            attemptLimiter.record(ipKey);
            throw e;
        }

        attemptLimiter.reset(emailKey);
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        return new LoginResponse(
                jwtService.generateToken(user),
                TOKEN_TYPE,
                jwtService.getExpirationSeconds(),
                user.id(),
                user.name(),
                user.role(),
                user.mustChangePassword()
        );
    }

    private Authentication authenticate(String email, String password) {
        try {
            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));
        } catch (DisabledException e) {
            throw new AccountDisabledException();
        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException();
        }
    }
}
