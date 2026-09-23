package com.mechanicmanager.appointments.service;

import com.mechanicmanager.appointments.exception.InvalidAppointmentStatusTransitionException;
import com.mechanicmanager.appointments.mapper.AppointmentMapper;
import com.mechanicmanager.appointments.model.Appointment;
import com.mechanicmanager.appointments.model.AppointmentStatus;
import com.mechanicmanager.appointments.repository.AppointmentRepository;
import com.mechanicmanager.shared.exception.ResourceNotFoundException;
import com.mechanicmanager.workorders.service.CreateWorkOrderFromAppointmentUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.mechanicmanager.appointments.service.AppointmentFixtures.appointmentWith;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompleteAppointmentUseCaseTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private CreateWorkOrderFromAppointmentUseCase createWorkOrderFromAppointmentUseCase;

    private CompleteAppointmentUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CompleteAppointmentUseCase(
                appointmentRepository,
                new AppointmentMapper(),
                createWorkOrderFromAppointmentUseCase
        );
    }

    @Test
    void completesScheduledAppointment() {
        Appointment appointment = appointmentWith(AppointmentStatus.SCHEDULED);

        when(appointmentRepository.findByIdAndUserId(50L, 7L))
                .thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = useCase.execute(7L, 50L);

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.COMPLETED);
        assertThat(response.status()).isEqualTo(AppointmentStatus.COMPLETED);
        assertThat(response.licensePlate()).isEqualTo("AB123CD");
    }

    /**
     * COMPLETED es terminal: completar dos veces no es idempotente, es un
     * error del que llama.
     */
    @Test
    void rejectsCompletingAlreadyCompletedAppointment() {
        when(appointmentRepository.findByIdAndUserId(50L, 7L))
                .thenReturn(Optional.of(appointmentWith(AppointmentStatus.COMPLETED)));

        assertThatThrownBy(() -> useCase.execute(7L, 50L))
                .isInstanceOf(InvalidAppointmentStatusTransitionException.class)
                .hasMessageContaining("COMPLETED");

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void rejectsCompletingCancelledAppointment() {
        when(appointmentRepository.findByIdAndUserId(50L, 7L))
                .thenReturn(Optional.of(appointmentWith(AppointmentStatus.CANCELLED)));

        assertThatThrownBy(() -> useCase.execute(7L, 50L))
                .isInstanceOf(InvalidAppointmentStatusTransitionException.class)
                .hasMessageContaining("CANCELLED");

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void rejectsAppointmentFromAnotherUser() {
        when(appointmentRepository.findByIdAndUserId(50L, 7L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(7L, 50L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No se encontró el turno indicado");

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }
}
