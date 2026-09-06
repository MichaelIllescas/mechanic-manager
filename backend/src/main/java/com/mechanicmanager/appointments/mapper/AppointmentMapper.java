package com.mechanicmanager.appointments.mapper;

import com.mechanicmanager.appointments.dto.AppointmentResponse;
import com.mechanicmanager.appointments.model.Appointment;
import com.mechanicmanager.customers.model.Customer;
import com.mechanicmanager.vehicles.model.Vehicle;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMapper {

    public AppointmentResponse toResponse(Appointment appointment) {
        Vehicle vehicle = appointment.getVehicle();
        Customer customer = vehicle.getCustomer();

        return new AppointmentResponse(
                appointment.getId(),
                appointment.getScheduledAt(),
                appointment.getStatus(),
                appointment.getNotes(),
                vehicle.getId(),
                vehicle.getLicensePlate(),
                vehicle.getBrand(),
                vehicle.getModel(),
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                appointment.getCreatedAt(),
                appointment.getUpdatedAt()
        );
    }
}
