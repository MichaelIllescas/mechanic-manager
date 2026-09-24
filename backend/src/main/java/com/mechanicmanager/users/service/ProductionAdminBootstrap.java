package com.mechanicmanager.users.service;

import com.mechanicmanager.users.model.Role;
import com.mechanicmanager.users.model.User;
import com.mechanicmanager.users.model.UserStatus;
import com.mechanicmanager.users.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Deja la cuenta SYSTEM_ADMIN de producción bajo control del operador.
 *
 * <p>La migración V2 siembra un administrador con una contraseña que es
 * pública en el repositorio. Esa migración no se puede editar sin invalidar
 * su checksum en las bases ya migradas, así que en producción se corrige acá,
 * una vez aplicadas las migraciones: se crea el administrador real a partir de
 * las variables de entorno y se deshabilita la cuenta sembrada.
 *
 * <p>Es idempotente: en los arranques siguientes no crea nada ni pisa una
 * contraseña que el administrador ya haya cambiado.
 */
@Slf4j
@Component
@Profile("prod")
public class ProductionAdminBootstrap implements ApplicationRunner {

    static final String SEEDED_ADMIN_EMAIL = "admin@mechanicmanager.local";
    static final int MIN_PASSWORD_LENGTH = 8;

    private static final String ADMIN_NAME = "Administrador";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public ProductionAdminBootstrap(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap-admin.email}") String adminEmail,
            @Value("${app.bootstrap-admin.password}") String adminPassword
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail.trim().toLowerCase();
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (adminEmail.isBlank() || SEEDED_ADMIN_EMAIL.equals(adminEmail)) {
            throw new IllegalStateException(
                    "ADMIN_EMAIL debe definirse y no puede ser la cuenta sembrada por las migraciones");
        }

        // Primero se asegura el administrador real y recién después se
        // deshabilita el sembrado, para no dejar el sistema sin acceso.
        if (!userRepository.existsByEmail(adminEmail)) {
            createAdmin();
        }

        disableSeededAdmin();
    }

    private void createAdmin() {
        if (adminPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalStateException(
                    "ADMIN_PASSWORD debe tener al menos " + MIN_PASSWORD_LENGTH + " caracteres");
        }

        userRepository.save(User.builder()
                .name(ADMIN_NAME)
                .email(adminEmail)
                .passwordHash(passwordEncoder.encode(adminPassword))
                .role(Role.SYSTEM_ADMIN)
                .status(UserStatus.ENABLED)
                .mustChangePassword(false)
                .build());

        log.info("Administrador inicial creado a partir de la configuracion del entorno");
    }

    private void disableSeededAdmin() {
        userRepository.findByEmail(SEEDED_ADMIN_EMAIL)
                .filter(User::isEnabled)
                .ifPresent(seeded -> {
                    seeded.setStatus(UserStatus.DISABLED);
                    userRepository.save(seeded);
                    log.warn("Se deshabilito la cuenta de administrador sembrada por las migraciones");
                });
    }
}
