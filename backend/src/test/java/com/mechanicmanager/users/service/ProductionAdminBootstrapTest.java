package com.mechanicmanager.users.service;

import com.mechanicmanager.users.model.Role;
import com.mechanicmanager.users.model.User;
import com.mechanicmanager.users.model.UserStatus;
import com.mechanicmanager.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductionAdminBootstrapTest {

    private static final String ADMIN_EMAIL = "owner@taller.com";
    private static final String SEEDED = ProductionAdminBootstrap.SEEDED_ADMIN_EMAIL;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private ProductionAdminBootstrap bootstrap(String email, String password) {
        return new ProductionAdminBootstrap(userRepository, passwordEncoder, email, password);
    }

    @Test
    void createsConfiguredAdminAndDisablesSeededAccount() {
        User seeded = User.builder()
                .email(SEEDED)
                .role(Role.SYSTEM_ADMIN)
                .status(UserStatus.ENABLED)
                .build();

        when(userRepository.existsByEmail(ADMIN_EMAIL)).thenReturn(false);
        when(passwordEncoder.encode("clave-segura-1")).thenReturn("hash");
        when(userRepository.findByEmail(SEEDED)).thenReturn(Optional.of(seeded));

        bootstrap("  OWNER@taller.com ", "clave-segura-1").run(null);

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository, org.mockito.Mockito.times(2)).save(saved.capture());

        User created = saved.getAllValues().get(0);
        assertThat(created.getEmail()).isEqualTo(ADMIN_EMAIL);
        assertThat(created.getRole()).isEqualTo(Role.SYSTEM_ADMIN);
        assertThat(created.getPasswordHash()).isEqualTo("hash");
        assertThat(created.isEnabled()).isTrue();
        assertThat(seeded.getStatus()).isEqualTo(UserStatus.DISABLED);
    }

    @Test
    void doesNotRecreateAdminNorOverwriteItsPasswordOnLaterStarts() {
        when(userRepository.existsByEmail(ADMIN_EMAIL)).thenReturn(true);
        when(userRepository.findByEmail(SEEDED)).thenReturn(Optional.empty());

        bootstrap(ADMIN_EMAIL, "clave-segura-1").run(null);

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void failsWhenEmailIsTheSeededAccount() {
        assertThatThrownBy(() -> bootstrap(SEEDED, "clave-segura-1").run(null))
                .isInstanceOf(IllegalStateException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void failsWhenPasswordIsTooShort() {
        when(userRepository.existsByEmail(ADMIN_EMAIL)).thenReturn(false);

        assertThatThrownBy(() -> bootstrap(ADMIN_EMAIL, "corta").run(null))
                .isInstanceOf(IllegalStateException.class);

        verify(userRepository, never()).save(any(User.class));
    }
}
