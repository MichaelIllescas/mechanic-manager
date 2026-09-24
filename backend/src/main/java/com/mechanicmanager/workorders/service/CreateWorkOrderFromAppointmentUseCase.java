package com.mechanicmanager.workorders.service;

import com.mechanicmanager.appointments.model.Appointment;
import com.mechanicmanager.quotes.model.Quote;
import com.mechanicmanager.quotes.model.QuoteItem;
import com.mechanicmanager.quotes.model.QuoteStatus;
import com.mechanicmanager.quotes.repository.QuoteRepository;
import com.mechanicmanager.workorders.model.WorkOrder;
import com.mechanicmanager.workorders.model.WorkOrderItem;
import com.mechanicmanager.workorders.model.WorkOrderStatus;
import com.mechanicmanager.workorders.repository.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Crea la orden de trabajo cuando un turno pasa a {@code COMPLETED}.
 *
 * <p>Si el turno tiene un presupuesto aprobado asociado, sus ítems se copian
 * a la orden (no se reutilizan) para que el presupuesto quede intacto como
 * referencia histórica. Si no hay presupuesto aprobado, la orden se crea
 * vacía para completarla a mano.
 */
@Service
@RequiredArgsConstructor
public class CreateWorkOrderFromAppointmentUseCase {

    private final WorkOrderRepository workOrderRepository;
    private final QuoteRepository quoteRepository;

    @Transactional
    public void execute(Long userId, Appointment appointment) {
        // Un turno origina una sola orden: si ya se cargó una a mano para este
        // turno, no se crea otra.
        if (workOrderRepository.existsByAppointmentIdAndUserId(appointment.getId(), userId)) {
            return;
        }

        // Un presupuesto también alimenta una sola orden: si ya se usó en otra,
        // la de este turno nace vacía.
        Quote quote = quoteRepository
                .findByAppointmentIdAndUserId(appointment.getId(), userId)
                .filter(candidate -> candidate.getStatus() == QuoteStatus.APPROVED)
                .filter(candidate -> !workOrderRepository
                        .existsByQuoteIdAndUserId(candidate.getId(), userId))
                .orElse(null);

        WorkOrder workOrder = WorkOrder.builder()
                .user(appointment.getUser())
                .vehicle(appointment.getVehicle())
                .appointment(appointment)
                .quote(quote)
                .performedAt(LocalDate.now())
                .status(WorkOrderStatus.COMPLETED)
                .total(BigDecimal.ZERO)
                .build();

        workOrder.replaceItems(itemsFrom(quote));

        workOrderRepository.save(workOrder);
    }

    private List<WorkOrderItem> itemsFrom(Quote quote) {
        if (quote == null) {
            return List.of();
        }

        return quote.getItems().stream()
                .map(this::copyItem)
                .toList();
    }

    private WorkOrderItem copyItem(QuoteItem source) {
        return WorkOrderItem.builder()
                .serviceType(source.getServiceType())
                .supply(source.getSupply())
                .description(source.getDescription())
                .quantity(source.getQuantity())
                .unitPrice(source.getUnitPrice())
                .subtotal(source.getSubtotal())
                .build();
    }
}
