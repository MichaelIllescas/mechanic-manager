package com.mechanicmanager.appointments.service;

import com.mechanicmanager.appointments.exception.InvalidAppointmentStatusTransitionException;
import com.mechanicmanager.appointments.mapper.AppointmentMapper;
import com.mechanicmanager.appointments.model.Appointment;
import com.mechanicmanager.appointments.model.AppointmentStatus;
import com.mechanicmanager.appointments.repository.AppointmentRepository;
import com.mechanicmanager.shared.exception.ResourceNotFoundException;
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
class CancelAppointmentUseCaseTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    private CancelAppointmentUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CancelAppointmentUseCase(
                appointmentRepository,
                new AppointmentMapper()
        );
    }

    /**
     * Cancelar conserva el turno: queda en el historial del vehículo.
     */
    @Test
    void cancelsScheduledAppointmentWithoutDeletingIt() {
        Appointment appointment = appointmentWith(AppointmentStatus.SCHEDULED);

        when(appointmentRepository.findByIdAndUserId(50L, 7L))
                .thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = useCase.execute(7L, 50L);

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
        assertThat(response.status()).isEqualTo(AppointmentStatus.CANCELLED);
        verify(appointmentRepository, never()).delete(any(Appointment.class));
    }

    @Test
    void rejectsCancellingCompletedAppointment() {
        when(appointmentRepository.findByIdAndUserId(50L, 7L))
                .thenReturn(Optional.of(appointmentWith(AppointmentStatus.COMPLETED)));

        assertThatThrownBy(() -> useCase.execute(7L, 50L))
                .isInstanceOf(InvalidAppointmentStatusTransitionException.class)
                .hasMessageContaining("COMPLETED");

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void rejectsCancellingAlreadyCancelledAppointment() {
        when(appointmentRepository.findByIdAndUserId(50L, 7L))
                .thenReturn(Optional.of(appointmentWith(AppointmentStatus.CANCELLED)));

        assertThatThrownBy(() -> useCase.execute(7L, 50L))
                .isInstanceOf(InvalidAppointmentStatusTransitionException.class);

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
