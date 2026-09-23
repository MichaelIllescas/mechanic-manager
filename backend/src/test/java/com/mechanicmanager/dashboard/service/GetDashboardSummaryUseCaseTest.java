package com.mechanicmanager.dashboard.service;

import com.mechanicmanager.appointments.model.Appointment;
import com.mechanicmanager.appointments.model.AppointmentStatus;
import com.mechanicmanager.appointments.repository.AppointmentRepository;
import com.mechanicmanager.customers.model.Customer;
import com.mechanicmanager.quotes.model.QuoteStatus;
import com.mechanicmanager.quotes.repository.QuoteRepository;
import com.mechanicmanager.vehicles.model.Vehicle;
import com.mechanicmanager.vehicles.repository.VehicleRepository;
import com.mechanicmanager.workorders.model.WorkOrderStatus;
import com.mechanicmanager.workorders.repository.WorkOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetDashboardSummaryUseCaseTest {

    private static final ZoneId ZONE = ZoneId.of("America/Argentina/Buenos_Aires");
    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-09-23T13:30:00Z"),
            ZONE
    );

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private QuoteRepository quoteRepository;

    @Mock
    private WorkOrderRepository workOrderRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    private GetDashboardSummaryUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetDashboardSummaryUseCase(
                appointmentRepository,
                quoteRepository,
                workOrderRepository,
                vehicleRepository,
                CLOCK
        );
    }

    @Test
    void buildsSummaryForAuthenticatedUsersCurrentDayAndMonth() {
        Long userId = 7L;
        LocalDateTime startOfToday = LocalDateTime.of(2026, 9, 23, 0, 0);
        LocalDateTime startOfTomorrow = LocalDateTime.of(2026, 9, 24, 0, 0);
        LocalDate startOfMonth = LocalDate.of(2026, 9, 1);
        LocalDate startOfNextMonth = LocalDate.of(2026, 10, 1);
        Appointment appointment = appointment();

        when(appointmentRepository
                .countByUserIdAndScheduledAtGreaterThanEqualAndScheduledAtLessThan(
                        userId, startOfToday, startOfTomorrow))
                .thenReturn(4L);
        when(appointmentRepository
                .findTop6ByUserIdAndScheduledAtGreaterThanEqualAndScheduledAtLessThanOrderByScheduledAtAsc(
                        userId, startOfToday, startOfTomorrow))
                .thenReturn(List.of(appointment));
        when(quoteRepository.countByUserIdAndStatus(userId, QuoteStatus.PENDING))
                .thenReturn(3L);
        when(quoteRepository.countByUserIdAndStatusAndValidUntilBefore(
                userId, QuoteStatus.PENDING, LocalDate.of(2026, 9, 23)))
                .thenReturn(1L);
        when(workOrderRepository
                .countByUserIdAndStatusAndPerformedAtGreaterThanEqualAndPerformedAtLessThan(
                        userId, WorkOrderStatus.COMPLETED, startOfMonth, startOfNextMonth))
                .thenReturn(8L);
        when(vehicleRepository.countByUserId(userId)).thenReturn(42L);

        var result = useCase.execute(userId);

        assertThat(result.generatedAt()).isEqualTo(LocalDateTime.of(2026, 9, 23, 10, 30));
        assertThat(result.metrics().todayAppointments()).isEqualTo(4L);
        assertThat(result.metrics().pendingQuotes()).isEqualTo(3L);
        assertThat(result.metrics().completedWorkOrdersThisMonth()).isEqualTo(8L);
        assertThat(result.metrics().registeredVehicles()).isEqualTo(42L);
        assertThat(result.alerts().expiredPendingQuotes()).isEqualTo(1L);
        assertThat(result.todayAppointments()).singleElement().satisfies(item -> {
            assertThat(item.id()).isEqualTo(50L);
            assertThat(item.licensePlate()).isEqualTo("AB123CD");
            assertThat(item.vehicleDescription()).isEqualTo("Toyota Corolla");
            assertThat(item.customerName()).isEqualTo("Ana Gómez");
        });
    }

    @Test
    void scopesEveryAggregateToTheAuthenticatedUser() {
        when(appointmentRepository
                .findTop6ByUserIdAndScheduledAtGreaterThanEqualAndScheduledAtLessThanOrderByScheduledAtAsc(
                        19L,
                        LocalDateTime.of(2026, 9, 23, 0, 0),
                        LocalDateTime.of(2026, 9, 24, 0, 0)))
                .thenReturn(List.of());

        useCase.execute(19L);

        verify(quoteRepository).countByUserIdAndStatus(19L, QuoteStatus.PENDING);
        verify(vehicleRepository).countByUserId(19L);
    }

    private static Appointment appointment() {
        Customer customer = Customer.builder()
                .id(20L)
                .firstName("Ana")
                .lastName("Gómez")
                .build();
        Vehicle vehicle = Vehicle.builder()
                .id(30L)
                .customer(customer)
                .licensePlate("AB123CD")
                .brand("Toyota")
                .model("Corolla")
                .build();

        return Appointment.builder()
                .id(50L)
                .vehicle(vehicle)
                .scheduledAt(LocalDateTime.of(2026, 9, 23, 11, 0))
                .status(AppointmentStatus.SCHEDULED)
                .build();
    }
}
