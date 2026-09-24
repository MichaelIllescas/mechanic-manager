package com.mechanicmanager.shared.security;

import com.mechanicmanager.users.model.Role;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static String base64Of(int bytes) {
        return Base64.getEncoder().encodeToString(new byte[bytes]);
    }

    private static AuthenticatedUser user() {
        return new AuthenticatedUser(7L, "Ana", "ana@example.com", "hash", Role.USER, true, false);
    }

    @Test
    void issuesATokenThatItCanReadBack() {
        JwtService service = new JwtService(base64Of(32), 60);

        String token = service.generateToken(user());

        assertThat(service.extractEmail(token)).contains("ana@example.com");
    }

    @Test
    void rejectsATokenSignedWithAnotherKey() {
        String token = new JwtService(base64Of(32), 60).generateToken(user());
        JwtService other = new JwtService(Base64.getEncoder().encodeToString(new byte[]{1, 2, 3, 4, 5, 6, 7, 8,
                1, 2, 3, 4, 5, 6, 7, 8, 1, 2, 3, 4, 5, 6, 7, 8, 1, 2, 3, 4, 5, 6, 7, 8}), 60);

        assertThat(other.extractEmail(token)).isEmpty();
    }

    @Test
    void aMissingSecretFailsWithAMessageThatSaysWhatToDo() {
        assertThatThrownBy(() -> new JwtService("", 60))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Falta JWT_SECRET")
                .hasMessageContaining(".env.example");
        assertThatThrownBy(() -> new JwtService("   ", 60))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Falta JWT_SECRET");
    }

    @Test
    void aSecretThatIsNotBase64IsExplained() {
        assertThatThrownBy(() -> new JwtService("esto no es base64 !!", 60))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET no es válido");
    }

    @Test
    void aSecretShorterThan32BytesIsExplained() {
        assertThatThrownBy(() -> new JwtService(base64Of(16), 60))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("al menos 32 bytes");
    }
}
