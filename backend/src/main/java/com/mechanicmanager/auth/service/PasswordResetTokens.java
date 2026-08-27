package com.mechanicmanager.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Generacion y hasheo de los tokens de recuperacion de contrasena.
 *
 * El token viaja en el email en claro y se guarda unicamente hasheado,
 * igual que una contrasena.
 */
final class PasswordResetTokens {
    
    private static final int TOKEN_BYTES = 32;
    private static final SecureRandom RANDOM = new SecureRandom();
    
    private PasswordResetTokens() {
    }
    
    /** Token aleatorio de 256 bits, apto para viajar en una URL. */
    static String generate() {
        byte[] bytes = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    
    /** SHA-256 en hexadecimal (64 caracteres), tal como se persiste. */
    static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible en la JVM", e);
        }
    }
}
