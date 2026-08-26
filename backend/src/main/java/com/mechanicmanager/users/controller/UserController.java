package com.mechanicmanager.users.controller;

import com.mechanicmanager.shared.security.AuthenticatedUser;
import com.mechanicmanager.users.dto.CreateUserRequest;
import com.mechanicmanager.users.dto.UpdateUserRequest;
import com.mechanicmanager.users.dto.UpdateUserStatusRequest;
import com.mechanicmanager.users.dto.UserResponse;
import com.mechanicmanager.users.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping(UserRoutes.BASE)
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
@RequiredArgsConstructor
public class UserController implements UserApi {
    
    private final UserService userService;
    
    @Override
    @GetMapping
    public ResponseEntity<List<UserResponse>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }
    
    @Override
    @GetMapping(UserRoutes.BY_ID)
    public ResponseEntity<UserResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }
    
    @Override
    @PostMapping
    public ResponseEntity<UserResponse> create(
            @Valid @RequestBody CreateUserRequest request,
            UriComponentsBuilder uriBuilder
    ) {
        UserResponse created = userService.create(request);
        
        return ResponseEntity
                .created(uriBuilder.path(UserRoutes.BASE + UserRoutes.BY_ID)
                        .buildAndExpand(created.id())
                        .toUri())
                .body(created);
    }
    
    @Override
    @PutMapping(UserRoutes.BY_ID)
    public ResponseEntity<UserResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        return ResponseEntity.ok(userService.update(id, request));
    }
    
    @Override
    @PatchMapping(UserRoutes.STATUS)
    public ResponseEntity<UserResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(userService.updateStatus(id, request, currentUser.id()));
    }
}
