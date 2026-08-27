package com.mechanicmanager.shared.email;

import java.util.Map;

/**
 * Mensaje a enviar. La plantilla se resuelve contra templates/email/{template}.html
 * y las variables se inyectan en ella.
 */
public record EmailMessage(
        String to,
        String subject,
        String template,
        Map<String, Object> variables
) {
}