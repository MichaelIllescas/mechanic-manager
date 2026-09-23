package com.mechanicmanager.dashboard.dto;

import com.mechanicmanager.appointments.model.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.List;

public record DashboardSummaryResponse(
        LocalDateTime generatedAt,
        Metrics metrics,
        Alerts alerts,
        List<TodayAppointment> todayAppointments
) {
    public record Metrics(
            long todayAppointments,
            long pendingQuotes,
            long completedWorkOrdersThisMonth,
            long registeredVehicles
    ) {
    }

    public record Alerts(long expiredPendingQuotes) {
    }

    public record TodayAppointment(
            Long id,
            LocalDateTime scheduledAt,
            AppointmentStatus status,
            String licensePlate,
            String vehicleDescription,
            String customerName
    ) {
    }
}
