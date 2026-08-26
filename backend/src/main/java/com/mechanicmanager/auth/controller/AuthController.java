package com.mechanicmanager.auth.controller;

import com.mechanicmanager.auth.dto.LoginRequest;
import com.mechanicmanager.auth.dto.LoginResponse;
import com.mechanicmanager.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AuthRoutes.BASE)
@RequiredArgsConstructor
public class AuthController implements AuthApi {
    
    private final AuthService authService;
    
    @Override
    @PostMapping(AuthRoutes.LOGIN)
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}