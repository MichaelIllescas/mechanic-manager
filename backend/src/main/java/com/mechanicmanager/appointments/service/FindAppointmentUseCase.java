package com.mechanicmanager.appointments.service;

import com.mechanicmanager.appointments.dto.AppointmentResponse;
import com.mechanicmanager.appointments.mapper.AppointmentMapper;
import com.mechanicmanager.appointments.repository.AppointmentRepository;
import com.mechanicmanager.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindAppointmentUseCase {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;

    /**
     * Un turno por id. La agenda se consulta por rango, pero reprogramar parte
     * de un identificador: sin esto, entrar directo a la URL de edición no
     * tendría de dónde cargar el turno.
     */
    @Transactional(readOnly = true)
    public AppointmentResponse execute(Long userId, Long appointmentId) {
        return appointmentRepository
                .findByIdAndUserId(appointmentId, userId)
                .map(appointmentMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el turno indicado"));
    }
}
