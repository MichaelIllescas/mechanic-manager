package com.mechanicmanager.shared.security;

import com.mechanicmanager.users.model.Role;
import com.mechanicmanager.users.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public record AuthenticatedUser(
        Long id,
        String name,
        String email,
        String passwordHash,
        Role role,
        boolean enabled,
        boolean mustChangePassword
) implements UserDetails {
    
    public static AuthenticatedUser fromEntity(User user) {
        return new AuthenticatedUser(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getRole(),
                user.isEnabled(),
                user.isMustChangePassword()
        );
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
    
    @Override
    public String getPassword() {
        return passwordHash;
    }
    
    @Override
    public String getUsername() {
        return email;
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
}