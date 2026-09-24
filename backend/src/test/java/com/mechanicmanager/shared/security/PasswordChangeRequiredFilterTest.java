package com.mechanicmanager.shared.security;

import com.mechanicmanager.users.model.Role;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class PasswordChangeRequiredFilterTest {

    private final PasswordChangeRequiredFilter filter =
            new PasswordChangeRequiredFilter(JsonMapper.builder().build());
    private final FilterChain chain = mock(FilterChain.class);
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        response = new MockHttpServletResponse();
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(boolean mustChangePassword) {
        AuthenticatedUser user = new AuthenticatedUser(
                1L, "Ana", "ana@example.com", "hash", Role.USER, true, mustChangePassword);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
    }

    @Test
    void blocksEveryEndpointWhilePasswordChangeIsPending() throws Exception {
        authenticateAs(true);

        filter.doFilter(new MockHttpServletRequest("GET", "/api/v1/customers"), response, chain);

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString()).contains("PASSWORD_CHANGE_REQUIRED");
        verify(chain, never()).doFilter(
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void allowsChangingThePasswordWhilePending() throws Exception {
        authenticateAs(true);
        MockHttpServletRequest request =
                new MockHttpServletRequest("POST", "/api/v1/auth/change-password");

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        verify(chain).doFilter(request, response);
    }

    @Test
    void doesNotAllowOtherMethodsOnTheChangePasswordPath() throws Exception {
        authenticateAs(true);

        filter.doFilter(
                new MockHttpServletRequest("GET", "/api/v1/auth/change-password"), response, chain);

        assertThat(response.getStatus()).isEqualTo(403);
    }

    @Test
    void letsThroughAccountsWithoutPendingChange() throws Exception {
        authenticateAs(false);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/customers");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void letsThroughAnonymousRequests() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }
}
