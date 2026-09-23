package com.mechanicmanager.appointments.service;

import com.mechanicmanager.appointments.model.Appointment;
import com.mechanicmanager.appointments.repository.AppointmentRepository;
import com.mechanicmanager.quotes.repository.QuoteRepository;
import com.mechanicmanager.shared.exception.ResourceNotFoundException;
import com.mechanicmanager.workorders.repository.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Baja definitiva: el presupuesto vinculado (si lo hay) y la orden de trabajo
 * autogenerada quedan sin turno (quotes.appointment_id y
 * work_orders.appointment_id son {@code ON DELETE SET NULL}), no se borran.
 */
@Service
@RequiredArgsConstructor
public class DeleteAppointmentUseCase {

    private final AppointmentRepository appointmentRepository;
    private final QuoteRepository quoteRepository;
    private final WorkOrderRepository workOrderRepository;

    @Transactional
    public void execute(Long userId, Long appointmentId) {
        Appointment appointment = appointmentRepository
                .findByIdAndUserId(appointmentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el turno indicado"));

        quoteRepository.findByAppointmentIdAndUserId(appointmentId, userId)
                .ifPresent(quote -> quote.setAppointment(null));
        workOrderRepository.findByAppointmentIdAndUserId(appointmentId, userId)
                .ifPresent(workOrder -> workOrder.setAppointment(null));

        appointmentRepository.flush();
        appointmentRepository.delete(appointment);
    }
}
