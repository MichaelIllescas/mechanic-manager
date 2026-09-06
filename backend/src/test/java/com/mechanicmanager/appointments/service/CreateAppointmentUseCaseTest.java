package com.mechanicmanager.appointments.service;

import com.mechanicmanager.appointments.dto.CreateAppointmentRequest;
import com.mechanicmanager.appointments.mapper.AppointmentMapper;
import com.mechanicmanager.appointments.model.Appointment;
import com.mechanicmanager.appointments.model.AppointmentStatus;
import com.mechanicmanager.appointments.repository.AppointmentRepository;
import com.mechanicmanager.customers.model.Customer;
import com.mechanicmanager.shared.exception.ResourceNotFoundException;
import com.mechanicmanager.users.model.User;
import com.mechanicmanager.users.repository.UserRepository;
import com.mechanicmanager.vehicles.model.Vehicle;
import com.mechanicmanager.vehicles.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateAppointmentUseCaseTest {

    private static final LocalDateTime SCHEDULED_AT =
            LocalDateTime.of(2026, 9, 10, 10, 30);

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private UserRepository userRepository;

    private CreateAppointmentUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateAppointmentUseCase(
                appointmentRepository,
                vehicleRepository,
                userRepository,
                new AppointmentMapper()
        );
    }

    @Test
    void createsScheduledAppointmentForVehicleFromAuthenticatedUser() {
        User user = User.builder().id(7L).build();
        Vehicle vehicle = vehicle(user);

        when(vehicleRepository.findByIdAndUserId(30L, 7L))
                .thenReturn(Optional.of(vehicle));
        when(userRepository.getReferenceById(7L)).thenReturn(user);
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment appointment = invocation.getArgument(0);
            appointment.setId(50L);
            return appointment;
        });

        var response = useCase.execute(7L, request(" Hace un ruido al frenar "));

        ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository).save(captor.capture());
        Appointment saved = captor.getValue();

        assertThat(saved.getUser()).isSameAs(user);
        assertThat(saved.getVehicle()).isSameAs(vehicle);
        assertThat(saved.getScheduledAt()).isEqualTo(SCHEDULED_AT);
        assertThat(saved.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
        assertThat(saved.getNotes()).isEqualTo("Hace un ruido al frenar");

        assertThat(response.id()).isEqualTo(50L);
        assertThat(response.vehicleId()).isEqualTo(30L);
        assertThat(response.licensePlate()).isEqualTo("AB123CD");
        assertThat(response.customerId()).isEqualTo(20L);
        assertThat(response.customerLastName()).isEqualTo("Pérez");
    }

    @Test
    void createsAppointmentWithoutNotes() {
        User user = User.builder().id(7L).build();

        when(vehicleRepository.findByIdAndUserId(30L, 7L))
                .thenReturn(Optional.of(vehicle(user)));
        when(userRepository.getReferenceById(7L)).thenReturn(user);
        when(appointmentRepository.save(any(Appointment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(7L, request("   "));

        ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository).save(captor.capture());

        assertThat(captor.getValue().getNotes()).isNull();
    }

    /**
     * El turno de un vehículo ajeno no es un 403: desde el usuario autenticado
     * ese vehículo no existe.
     */
    @Test
    void rejectsVehicleFromAnotherUser() {
        when(vehicleRepository.findByIdAndUserId(30L, 7L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(7L, request(null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No se encontró el vehículo indicado");

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    /**
     * Un turno pactado ayer por teléfono puede cargarse hoy: la fecha pasada
     * no se rechaza.
     */
    @Test
    void acceptsAppointmentScheduledInThePast() {
        User user = User.builder().id(7L).build();
        LocalDateTime past = LocalDateTime.of(2020, 1, 15, 9, 0);

        when(vehicleRepository.findByIdAndUserId(30L, 7L))
                .thenReturn(Optional.of(vehicle(user)));
        when(userRepository.getReferenceById(7L)).thenReturn(user);
        when(appointmentRepository.save(any(Appointment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(7L, new CreateAppointmentRequest(30L, past, null));

        ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository).save(captor.capture());

        assertThat(captor.getValue().getScheduledAt()).isEqualTo(past);
    }

    private static Vehicle vehicle(User user) {
        Customer customer = Customer.builder()
                .id(20L)
                .user(user)
                .firstName("Juan")
                .lastName("Pérez")
                .build();

        return Vehicle.builder()
                .id(30L)
                .user(user)
                .customer(customer)
                .licensePlate("AB123CD")
                .brand("Toyota")
                .model("Corolla")
                .build();
    }

    private static CreateAppointmentRequest request(String notes) {
        return new CreateAppointmentRequest(30L, SCHEDULED_AT, notes);
    }
}
