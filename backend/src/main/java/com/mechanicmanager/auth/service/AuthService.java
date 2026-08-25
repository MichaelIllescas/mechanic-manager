package com.mechanicmanager.auth.service;

import com.mechanicmanager.auth.dto.LoginRequest;
import com.mechanicmanager.auth.dto.LoginResponse;
import com.mechanicmanager.auth.exception.AccountDisabledException;
import com.mechanicmanager.auth.exception.InvalidCredentialsException;
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

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();

        Authentication authentication = authenticate(email, request.password());
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        return new LoginResponse(
                jwtService.generateToken(user),
                TOKEN_TYPE,
                jwtService.getExpirationSeconds(),
                user.id(),
                user.name(),
                user.role()
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
