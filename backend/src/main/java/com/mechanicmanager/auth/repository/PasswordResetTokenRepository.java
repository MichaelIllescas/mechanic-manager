package com.mechanicmanager.auth.repository;

import com.mechanicmanager.auth.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);
    
    /**
     * Invalida los pedidos anteriores del usuario: solo el ultimo enlace enviado sigue vigente.
     */
    @Modifying
    @Query("""
            UPDATE PasswordResetToken t
               SET t.usedAt = :now
             WHERE t.user.id = :userId
               AND t.usedAt IS NULL
            """)
    int invalidatePendingTokens(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}
