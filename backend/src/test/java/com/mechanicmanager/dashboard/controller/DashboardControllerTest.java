package com.mechanicmanager.dashboard.controller;

import com.mechanicmanager.dashboard.dto.DashboardSummaryResponse;
import com.mechanicmanager.dashboard.service.GetDashboardSummaryUseCase;
import com.mechanicmanager.shared.security.AuthenticatedUser;
import com.mechanicmanager.users.model.Role;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DashboardControllerTest {

    @Test
    void delegatesSummaryToTheAuthenticatedUser() {
        GetDashboardSummaryUseCase useCase = mock(GetDashboardSummaryUseCase.class);
        DashboardController controller = new DashboardController(useCase);
        AuthenticatedUser user = new AuthenticatedUser(
                7L, "Ana", "ana@example.com", "hash", Role.USER, true);
        DashboardSummaryResponse expected = new DashboardSummaryResponse(
                LocalDateTime.of(2026, 9, 23, 10, 30),
                new DashboardSummaryResponse.Metrics(0, 0, 0, 0),
                new DashboardSummaryResponse.Alerts(0),
                List.of()
        );
        when(useCase.execute(7L)).thenReturn(expected);

        var response = controller.summary(user);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isSameAs(expected);
        verify(useCase).execute(7L);
    }
}
