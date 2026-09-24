package com.mechanicmanager.shared.security;

import com.mechanicmanager.auth.controller.AuthRoutes;
import com.mechanicmanager.shared.exception.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Mientras una cuenta tenga la contraseña temporal que le puso el administrador,
 * lo único que puede hacer es cambiarla.
 *
 * <p>Va después de {@link JwtAuthenticationFilter}: necesita el usuario ya
 * autenticado. Vive en el servidor porque el frontend solo guía al usuario; quien
 * llame a la API directamente no pasa por sus pantallas.
 */
@Component
@RequiredArgsConstructor
public class PasswordChangeRequiredFilter extends OncePerRequestFilter {

    public static final String CODE = "PASSWORD_CHANGE_REQUIRED";

    private static final String CHANGE_PASSWORD_PATH =
            AuthRoutes.BASE + AuthRoutes.CHANGE_PASSWORD;

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        if (mustChangePassword() && !isChangePasswordRequest(request)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            objectMapper.writeValue(
                    response.getOutputStream(),
                    ErrorResponse.of(CODE, "Tenés que cambiar tu contraseña temporal para continuar"));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean mustChangePassword() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return authentication != null
                && authentication.getPrincipal() instanceof AuthenticatedUser user
                && user.mustChangePassword();
    }

    private boolean isChangePasswordRequest(HttpServletRequest request) {
        return HttpMethod.POST.matches(request.getMethod())
                && CHANGE_PASSWORD_PATH.equals(request.getRequestURI());
    }
}
