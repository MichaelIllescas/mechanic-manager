package com.mechanicmanager.auth.controller;

import com.mechanicmanager.auth.dto.ChangePasswordRequest;
import com.mechanicmanager.auth.dto.ForgotPasswordRequest;
import com.mechanicmanager.auth.dto.LoginRequest;
import com.mechanicmanager.auth.dto.LoginResponse;
import com.mechanicmanager.auth.dto.ResetPasswordRequest;
import com.mechanicmanager.auth.service.AuthService;
import com.mechanicmanager.auth.service.ChangePasswordUseCase;
import com.mechanicmanager.auth.service.RequestPasswordResetUseCase;
import com.mechanicmanager.auth.service.ResetPasswordUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.mechanicmanager.shared.security.AuthenticatedUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AuthRoutes.BASE)
@RequiredArgsConstructor
public class AuthController implements AuthApi {
    
    private final AuthService authService;
    private final RequestPasswordResetUseCase requestPasswordResetUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    
    @Override
    @PostMapping(AuthRoutes.LOGIN)
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
    
    @Override
    @PostMapping(AuthRoutes.FORGOT_PASSWORD)
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        requestPasswordResetUseCase.execute(request);
        
        return ResponseEntity.noContent().build();
    }
    
    @Override
    @PostMapping(AuthRoutes.RESET_PASSWORD)
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        resetPasswordUseCase.execute(request);
        
        return ResponseEntity.noContent().build();
    }
    
    @Override
    @PostMapping(AuthRoutes.CHANGE_PASSWORD)
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        changePasswordUseCase.execute(currentUser.id(), request);
        
        return ResponseEntity.noContent().build();
    }
}
