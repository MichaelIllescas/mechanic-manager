package com.mechanicmanager.workorders.service;

import com.mechanicmanager.appointments.model.Appointment;
import com.mechanicmanager.appointments.repository.AppointmentRepository;
import com.mechanicmanager.quotes.model.Quote;
import com.mechanicmanager.quotes.repository.QuoteRepository;
import com.mechanicmanager.shared.exception.ResourceNotFoundException;
import com.mechanicmanager.users.model.User;
import com.mechanicmanager.users.repository.UserRepository;
import com.mechanicmanager.vehicles.model.Vehicle;
import com.mechanicmanager.vehicles.repository.VehicleRepository;
import com.mechanicmanager.workorders.dto.CreateWorkOrderRequest;
import com.mechanicmanager.workorders.dto.WorkOrderResponse;
import com.mechanicmanager.workorders.exception.InvalidWorkOrderException;
import com.mechanicmanager.workorders.exception.WorkOrderAlreadyExistsException;
import com.mechanicmanager.workorders.mapper.WorkOrderMapper;
import com.mechanicmanager.workorders.model.WorkOrder;
import com.mechanicmanager.workorders.model.WorkOrderItem;
import com.mechanicmanager.workorders.model.WorkOrderStatus;
import com.mechanicmanager.workorders.repository.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateWorkOrderUseCase {

    private final WorkOrderRepository workOrderRepository;
    private final VehicleRepository vehicleRepository;
    private final AppointmentRepository appointmentRepository;
    private final QuoteRepository quoteRepository;
    private final UserRepository userRepository;
    private final WorkOrderItemFactory workOrderItemFactory;
    private final WorkOrderMapper workOrderMapper;

    @Transactional
    public WorkOrderResponse execute(Long userId, CreateWorkOrderRequest request) {
        User user = userRepository.findOneById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el usuario autenticado"
                ));

        Vehicle vehicle = vehicleRepository
                .findByIdAndUserId(request.vehicleId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el vehículo indicado"
                ));

        Appointment appointment = findAppointment(userId, request.appointmentId(), vehicle);
        Quote quote = findQuote(userId, request.quoteId(), vehicle);

        List<WorkOrderItem> items = workOrderItemFactory.createAll(userId, request.items());

        WorkOrder workOrder = WorkOrder.builder()
                .user(user)
                .vehicle(vehicle)
                .appointment(appointment)
                .quote(quote)
                .performedAt(request.performedAt())
                .mileage(request.mileage())
                .status(WorkOrderStatus.COMPLETED)
                .total(BigDecimal.ZERO)
                .notes(trimToNull(request.notes()))
                .build();

        workOrder.replaceItems(items);

        return workOrderMapper.toResponse(workOrderRepository.save(workOrder));
    }

    private Appointment findAppointment(Long userId, Long appointmentId, Vehicle vehicle) {
        if (appointmentId == null) {
            return null;
        }

        Appointment appointment = appointmentRepository
                .findByIdAndUserId(appointmentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el turno indicado"
                ));

        if (!appointment.getVehicle().getId().equals(vehicle.getId())) {
            throw new InvalidWorkOrderException(
                    "El turno indicado no pertenece al vehículo seleccionado"
            );
        }

        if (workOrderRepository.existsByAppointmentIdAndUserId(appointmentId, userId)) {
            throw new WorkOrderAlreadyExistsException(
                    "El turno indicado ya tiene un trabajo realizado asociado"
            );
        }

        return appointment;
    }

    private Quote findQuote(Long userId, Long quoteId, Vehicle vehicle) {
        if (quoteId == null) {
            return null;
        }

        Quote quote = quoteRepository.findByIdAndUserId(quoteId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el presupuesto indicado"
                ));

        if (!quote.getVehicle().getId().equals(vehicle.getId())) {
            throw new InvalidWorkOrderException(
                    "El presupuesto indicado no pertenece al vehículo seleccionado"
            );
        }

        if (workOrderRepository.existsByQuoteIdAndUserId(quoteId, userId)) {
            throw new WorkOrderAlreadyExistsException(
                    "El presupuesto indicado ya tiene un trabajo realizado asociado"
            );
        }

        return quote;
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
