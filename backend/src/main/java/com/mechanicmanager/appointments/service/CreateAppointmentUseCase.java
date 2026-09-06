package com.mechanicmanager.appointments.service;

import com.mechanicmanager.appointments.dto.AppointmentResponse;
import com.mechanicmanager.appointments.dto.CreateAppointmentRequest;
import com.mechanicmanager.appointments.mapper.AppointmentMapper;
import com.mechanicmanager.appointments.model.Appointment;
import com.mechanicmanager.appointments.model.AppointmentStatus;
import com.mechanicmanager.appointments.repository.AppointmentRepository;
import com.mechanicmanager.shared.exception.ResourceNotFoundException;
import com.mechanicmanager.users.repository.UserRepository;
import com.mechanicmanager.vehicles.model.Vehicle;
import com.mechanicmanager.vehicles.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateAppointmentUseCase {

    private final AppointmentRepository appointmentRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final AppointmentMapper appointmentMapper;

    /**
     * La agenda no valida solapamiento: el taller puede atender varios
     * vehículos en el mismo horario y el MVP no modela empleados, así que no
     * hay criterio para decidir qué es un conflicto.
     */
    @Transactional
    public AppointmentResponse execute(Long userId, CreateAppointmentRequest request) {
        Vehicle vehicle = vehicleRepository.findByIdAndUserId(
                        request.vehicleId(),
                        userId
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el vehículo indicado"));

        Appointment appointment = Appointment.builder()
                .user(userRepository.getReferenceById(userId))
                .vehicle(vehicle)
                .scheduledAt(request.scheduledAt())
                .status(AppointmentStatus.SCHEDULED)
                .notes(trimToNull(request.notes()))
                .build();

        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    private static String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
