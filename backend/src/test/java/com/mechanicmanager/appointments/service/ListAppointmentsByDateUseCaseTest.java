package com.mechanicmanager.appointments.service;

import com.mechanicmanager.appointments.mapper.AppointmentMapper;
import com.mechanicmanager.appointments.model.Appointment;
import com.mechanicmanager.appointments.model.AppointmentStatus;
import com.mechanicmanager.appointments.repository.AppointmentRepository;
import com.mechanicmanager.customers.model.Customer;
import com.mechanicmanager.users.model.User;
import com.mechanicmanager.vehicles.model.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListAppointmentsByDateUseCaseTest {

    private static final LocalDate DATE = LocalDate.of(2026, 9, 10);

    @Mock
    private AppointmentRepository appointmentRepository;

    private ListAppointmentsByDateUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListAppointmentsByDateUseCase(
                appointmentRepository,
                new AppointmentMapper()
        );
    }

    /**
     * El rango debe ser semiabierto: incluye la medianoche del día pedido y
     * excluye la del día siguiente, para que ningún turno quede fuera de la
     * agenda ni aparezca en dos días.
     */
    @Test
    void queriesTheDayAsAHalfOpenRange() {
        when(appointmentRepository
                .findAllByUserIdAndScheduledAtGreaterThanEqualAndScheduledAtLessThanOrderByScheduledAtAsc(
                        7L,
                        LocalDateTime.of(2026, 9, 10, 0, 0),
                        LocalDateTime.of(2026, 9, 11, 0, 0)
                ))
                .thenReturn(List.of(appointment(10L, LocalDateTime.of(2026, 9, 10, 8, 0))));

        var responses = useCase.execute(7L, DATE);

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().id()).isEqualTo(10L);
        assertThat(responses.getFirst().licensePlate()).isEqualTo("AB123CD");
        assertThat(responses.getFirst().customerLastName()).isEqualTo("Pérez");
    }

    @Test
    void returnsEmptyListWhenThereAreNoAppointments() {
        when(appointmentRepository
                .findAllByUserIdAndScheduledAtGreaterThanEqualAndScheduledAtLessThanOrderByScheduledAtAsc(
                        7L,
                        LocalDateTime.of(2026, 9, 10, 0, 0),
                        LocalDateTime.of(2026, 9, 11, 0, 0)
                ))
                .thenReturn(List.of());

        assertThat(useCase.execute(7L, DATE)).isEmpty();
    }

    private static Appointment appointment(Long id, LocalDateTime scheduledAt) {
        User user = User.builder().id(7L).build();

        Customer customer = Customer.builder()
                .id(20L)
                .user(user)
                .firstName("Juan")
                .lastName("Pérez")
                .build();

        Vehicle vehicle = Vehicle.builder()
                .id(30L)
                .user(user)
                .customer(customer)
                .licensePlate("AB123CD")
                .brand("Toyota")
                .model("Corolla")
                .build();

        return Appointment.builder()
                .id(id)
                .user(user)
                .vehicle(vehicle)
                .scheduledAt(scheduledAt)
                .status(AppointmentStatus.SCHEDULED)
                .build();
    }
}
