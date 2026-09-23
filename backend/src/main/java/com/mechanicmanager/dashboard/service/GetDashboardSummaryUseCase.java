package com.mechanicmanager.dashboard.service;

import com.mechanicmanager.appointments.model.Appointment;
import com.mechanicmanager.appointments.repository.AppointmentRepository;
import com.mechanicmanager.dashboard.dto.DashboardSummaryResponse;
import com.mechanicmanager.quotes.model.QuoteStatus;
import com.mechanicmanager.quotes.repository.QuoteRepository;
import com.mechanicmanager.vehicles.model.Vehicle;
import com.mechanicmanager.vehicles.repository.VehicleRepository;
import com.mechanicmanager.workorders.model.WorkOrderStatus;
import com.mechanicmanager.workorders.repository.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetDashboardSummaryUseCase {

    private final AppointmentRepository appointmentRepository;
    private final QuoteRepository quoteRepository;
    private final WorkOrderRepository workOrderRepository;
    private final VehicleRepository vehicleRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public DashboardSummaryResponse execute(Long userId) {
        LocalDateTime generatedAt = LocalDateTime.now(clock);
        LocalDate today = generatedAt.toLocalDate();
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime startOfTomorrow = today.plusDays(1).atStartOfDay();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate startOfNextMonth = startOfMonth.plusMonths(1);

        long todayAppointments = appointmentRepository
                .countByUserIdAndScheduledAtGreaterThanEqualAndScheduledAtLessThan(
                        userId,
                        startOfToday,
                        startOfTomorrow
                );
        long pendingQuotes = quoteRepository.countByUserIdAndStatus(userId, QuoteStatus.PENDING);
        long expiredPendingQuotes = quoteRepository
                .countByUserIdAndStatusAndValidUntilBefore(userId, QuoteStatus.PENDING, today);
        long completedWorkOrders = workOrderRepository
                .countByUserIdAndStatusAndPerformedAtGreaterThanEqualAndPerformedAtLessThan(
                        userId,
                        WorkOrderStatus.COMPLETED,
                        startOfMonth,
                        startOfNextMonth
                );
        long registeredVehicles = vehicleRepository.countByUserId(userId);

        var agenda = appointmentRepository
                .findTop6ByUserIdAndScheduledAtGreaterThanEqualAndScheduledAtLessThanOrderByScheduledAtAsc(
                        userId,
                        startOfToday,
                        startOfTomorrow
                )
                .stream()
                .map(this::toTodayAppointment)
                .toList();

        log.debug(
                "Dashboard summary generated: userId={}, date={}, appointments={}, pendingQuotes={}",
                userId,
                today,
                todayAppointments,
                pendingQuotes
        );

        return new DashboardSummaryResponse(
                generatedAt,
                new DashboardSummaryResponse.Metrics(
                        todayAppointments,
                        pendingQuotes,
                        completedWorkOrders,
                        registeredVehicles
                ),
                new DashboardSummaryResponse.Alerts(expiredPendingQuotes),
                agenda
        );
    }

    private DashboardSummaryResponse.TodayAppointment toTodayAppointment(Appointment appointment) {
        Vehicle vehicle = appointment.getVehicle();
        var customer = vehicle.getCustomer();

        return new DashboardSummaryResponse.TodayAppointment(
                appointment.getId(),
                appointment.getScheduledAt(),
                appointment.getStatus(),
                vehicle.getLicensePlate(),
                vehicle.getBrand() + " " + vehicle.getModel(),
                customer.getFirstName() + " " + customer.getLastName()
        );
    }
}
