package com.mechanicmanager.auth.service;

import com.mechanicmanager.auth.dto.ChangePasswordRequest;
import com.mechanicmanager.auth.exception.InvalidCurrentPasswordException;
import com.mechanicmanager.auth.exception.PasswordNotChangedException;
import com.mechanicmanager.users.model.User;
import com.mechanicmanager.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class ChangePasswordUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private User pendingUser() {
        return User.builder()
                .id(5L)
                .passwordHash("temp-hash")
                .mustChangePassword(true)
                .build();
    }

    @Test
    void changesPasswordAndClearsThePendingFlag() {
        User user = pendingUser();
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("temporal-123", "temp-hash")).thenReturn(true);
        when(passwordEncoder.encode("definitiva-456")).thenReturn("new-hash");

        new ChangePasswordUseCase(userRepository, passwordEncoder)
                .execute(5L, new ChangePasswordRequest("temporal-123", "definitiva-456"));

        assertThat(user.getPasswordHash()).isEqualTo("new-hash");
        assertThat(user.isMustChangePassword()).isFalse();
        verify(userRepository).save(user);
    }

    @Test
    void rejectsWrongCurrentPassword() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(pendingUser()));
        when(passwordEncoder.matches("mala", "temp-hash")).thenReturn(false);

        assertThatThrownBy(() -> new ChangePasswordUseCase(userRepository, passwordEncoder)
                .execute(5L, new ChangePasswordRequest("mala", "definitiva-456")))
                .isInstanceOf(InvalidCurrentPasswordException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void rejectsKeepingTheSamePassword() {
        User user = pendingUser();
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("temporal-123", "temp-hash")).thenReturn(true);

        assertThatThrownBy(() -> new ChangePasswordUseCase(userRepository, passwordEncoder)
                .execute(5L, new ChangePasswordRequest("temporal-123", "temporal-123")))
                .isInstanceOf(PasswordNotChangedException.class);

        assertThat(user.isMustChangePassword()).isTrue();
        verify(userRepository, never()).save(any(User.class));
    }
}
