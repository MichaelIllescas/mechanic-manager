package com.mechanicmanager.appointments.service;

import com.mechanicmanager.appointments.dto.AppointmentResponse;
import com.mechanicmanager.appointments.dto.UpdateAppointmentRequest;
import com.mechanicmanager.appointments.exception.AppointmentNotEditableException;
import com.mechanicmanager.appointments.mapper.AppointmentMapper;
import com.mechanicmanager.appointments.model.Appointment;
import com.mechanicmanager.appointments.model.AppointmentStatus;
import com.mechanicmanager.appointments.repository.AppointmentRepository;
import com.mechanicmanager.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateAppointmentUseCase {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;

    /**
     * Reprogramar es modificar la fecha de un turno agendado: no es un estado
     * propio ni genera un turno nuevo.
     *
     * <p>Un turno completado o cancelado ya no se edita: su fecha es parte del
     * historial del vehículo.
     */
    @Transactional
    public AppointmentResponse execute(
            Long userId,
            Long appointmentId,
            UpdateAppointmentRequest request
    ) {
        Appointment appointment = appointmentRepository
                .findByIdAndUserId(appointmentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el turno indicado"));

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new AppointmentNotEditableException(appointment.getStatus());
        }

        appointment.setScheduledAt(request.scheduledAt());
        appointment.setNotes(trimToNull(request.notes()));

        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    private static String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
