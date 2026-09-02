package com.mechanicmanager.appointments.service;

import com.mechanicmanager.appointments.dto.AppointmentResponse;
import com.mechanicmanager.appointments.exception.InvalidAppointmentStatusTransitionException;
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
public class CompleteAppointmentUseCase {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;

    /**
     * El turno se completa cuando el vehículo ingresa al taller. A partir de
     * ahí el estado del trabajo lo lleva la orden de trabajo, no el turno.
     */
    @Transactional
    public AppointmentResponse execute(Long userId, Long appointmentId) {
        Appointment appointment = appointmentRepository
                .findByIdAndUserId(appointmentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el turno indicado"));

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new InvalidAppointmentStatusTransitionException(
                    appointment.getStatus(),
                    AppointmentStatus.COMPLETED
            );
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);

        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }
}
