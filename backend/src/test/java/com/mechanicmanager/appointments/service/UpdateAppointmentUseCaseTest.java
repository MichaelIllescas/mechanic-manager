package com.mechanicmanager.appointments.service;

import com.mechanicmanager.appointments.dto.UpdateAppointmentRequest;
import com.mechanicmanager.appointments.exception.AppointmentNotEditableException;
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

import java.time.LocalDateTime;
import java.util.Optional;

import static com.mechanicmanager.appointments.service.AppointmentFixtures.appointmentWith;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateAppointmentUseCaseTest {

    private static final LocalDateTime NEW_SCHEDULED_AT =
            LocalDateTime.of(2026, 9, 12, 15, 0);

    @Mock
    private AppointmentRepository appointmentRepository;

    private UpdateAppointmentUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateAppointmentUseCase(
                appointmentRepository,
                new AppointmentMapper()
        );
    }

    @Test
    void reschedulesScheduledAppointment() {
        Appointment appointment = appointmentWith(AppointmentStatus.SCHEDULED);

        when(appointmentRepository.findByIdAndUserId(50L, 7L))
                .thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = useCase.execute(7L, 50L, request(" Revisar frenos "));

        assertThat(appointment.getScheduledAt()).isEqualTo(NEW_SCHEDULED_AT);
        assertThat(appointment.getNotes()).isEqualTo("Revisar frenos");
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
        assertThat(response.scheduledAt()).isEqualTo(NEW_SCHEDULED_AT);
    }

    @Test
    void clearsNotesWhenBlank() {
        Appointment appointment = appointmentWith(AppointmentStatus.SCHEDULED);

        when(appointmentRepository.findByIdAndUserId(50L, 7L))
                .thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(7L, 50L, request("   "));

        assertThat(appointment.getNotes()).isNull();
    }

    /**
     * La fecha de un turno cerrado es parte del historial del vehículo.
     */
    @Test
    void rejectsReschedulingCompletedAppointment() {
        when(appointmentRepository.findByIdAndUserId(50L, 7L))
                .thenReturn(Optional.of(appointmentWith(AppointmentStatus.COMPLETED)));

        assertThatThrownBy(() -> useCase.execute(7L, 50L, request(null)))
                .isInstanceOf(AppointmentNotEditableException.class)
                .hasMessageContaining("COMPLETED");

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void rejectsReschedulingCancelledAppointment() {
        when(appointmentRepository.findByIdAndUserId(50L, 7L))
                .thenReturn(Optional.of(appointmentWith(AppointmentStatus.CANCELLED)));

        assertThatThrownBy(() -> useCase.execute(7L, 50L, request(null)))
                .isInstanceOf(AppointmentNotEditableException.class);

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void rejectsAppointmentFromAnotherUser() {
        when(appointmentRepository.findByIdAndUserId(50L, 7L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(7L, 50L, request(null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No se encontró el turno indicado");

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    private static UpdateAppointmentRequest request(String notes) {
        return new UpdateAppointmentRequest(NEW_SCHEDULED_AT, notes);
    }
}
