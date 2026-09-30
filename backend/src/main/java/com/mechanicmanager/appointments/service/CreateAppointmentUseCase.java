package com.mechanicmanager.appointments.service;

import com.mechanicmanager.appointments.dto.AppointmentResponse;
import com.mechanicmanager.appointments.dto.CreateAppointmentRequest;
import com.mechanicmanager.appointments.exception.InvalidAppointmentQuoteException;
import com.mechanicmanager.appointments.mapper.AppointmentMapper;
import com.mechanicmanager.appointments.model.Appointment;
import com.mechanicmanager.appointments.model.AppointmentStatus;
import com.mechanicmanager.appointments.repository.AppointmentRepository;
import com.mechanicmanager.quotes.model.Quote;
import com.mechanicmanager.quotes.model.QuoteStatus;
import com.mechanicmanager.quotes.repository.QuoteRepository;
import com.mechanicmanager.shared.exception.ResourceNotFoundException;
import com.mechanicmanager.shared.text.Texts;
import com.mechanicmanager.users.repository.UserRepository;
import com.mechanicmanager.vehicles.model.Vehicle;
import com.mechanicmanager.vehicles.repository.VehicleRepository;
import com.mechanicmanager.workorders.repository.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateAppointmentUseCase {

    private final AppointmentRepository appointmentRepository;
    private final VehicleRepository vehicleRepository;
    private final QuoteRepository quoteRepository;
    private final WorkOrderRepository workOrderRepository;
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
                .notes(Texts.trimToNull(request.notes()))
                .build();

        if (request.quoteId() != null) {
            appointment.setQuote(availableQuote(userId, vehicle, request.quoteId()));
        }

        return appointmentMapper.toResponse(appointmentRepository.save(appointment));
    }

    /**
     * Un presupuesto se puede vincular si está aprobado, es del vehículo, no
     * generó un trabajo realizado y ningún turno activo lo usa. Los turnos
     * cancelados lo conservan como historial pero no lo bloquean.
     */
    private Quote availableQuote(Long userId, Vehicle vehicle, Long quoteId) {
        Quote quote = quoteRepository.findByIdAndUserId(quoteId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el presupuesto indicado"));

        if (quote.getStatus() != QuoteStatus.APPROVED) {
            throw new InvalidAppointmentQuoteException(
                    "El presupuesto indicado no está aprobado");
        }

        if (!quote.getVehicle().getId().equals(vehicle.getId())) {
            throw new InvalidAppointmentQuoteException(
                    "El presupuesto indicado no pertenece al vehículo seleccionado");
        }

        if (workOrderRepository.existsByQuoteIdAndUserId(quoteId, userId)) {
            throw new InvalidAppointmentQuoteException(
                    "El presupuesto indicado ya tiene un trabajo realizado");
        }

        if (appointmentRepository.existsByQuoteIdAndUserIdAndStatusNot(
                quoteId, userId, AppointmentStatus.CANCELLED)) {
            throw new InvalidAppointmentQuoteException(
                    "El presupuesto indicado ya está vinculado a otro turno");
        }

        return quote;
    }
}
