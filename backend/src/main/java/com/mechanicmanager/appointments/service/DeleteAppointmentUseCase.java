package com.mechanicmanager.appointments.service;

import com.mechanicmanager.appointments.model.Appointment;
import com.mechanicmanager.appointments.repository.AppointmentRepository;
import com.mechanicmanager.shared.exception.ResourceNotFoundException;
import com.mechanicmanager.workorders.repository.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Baja definitiva: la orden de trabajo autogenerada queda sin turno
 * (work_orders.appointment_id es {@code ON DELETE SET NULL}), no se borra. El
 * presupuesto vinculado no se toca: la referencia la guarda el turno.
 */
@Service
@RequiredArgsConstructor
public class DeleteAppointmentUseCase {

    private final AppointmentRepository appointmentRepository;
    private final WorkOrderRepository workOrderRepository;

    @Transactional
    public void execute(Long userId, Long appointmentId) {
        Appointment appointment = appointmentRepository
                .findByIdAndUserId(appointmentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el turno indicado"));

        workOrderRepository.findByAppointmentIdAndUserId(appointmentId, userId)
                .ifPresent(workOrder -> workOrder.setAppointment(null));

        appointmentRepository.flush();
        appointmentRepository.delete(appointment);
    }
}
