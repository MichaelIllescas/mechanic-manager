package com.mechanicmanager.appointments.service;

import com.mechanicmanager.appointments.model.Appointment;
import com.mechanicmanager.appointments.model.AppointmentStatus;
import com.mechanicmanager.customers.model.Customer;
import com.mechanicmanager.users.model.User;
import com.mechanicmanager.vehicles.model.Vehicle;

import java.time.LocalDateTime;

/**
 * Turnos armados para los tests de los use cases que operan sobre un turno ya
 * existente. El vehículo trae su cliente porque el mapper lo lee para armar la
 * fila de la agenda.
 */
final class AppointmentFixtures {

    static final LocalDateTime SCHEDULED_AT = LocalDateTime.of(2026, 9, 10, 10, 30);

    private AppointmentFixtures() {
    }

    static Appointment appointmentWith(AppointmentStatus status) {
        User user = User.builder().id(7L).build();

        Customer customer = Customer.builder()
                .id(20L)
                .user(user)
                .firstName("Juan")
                .lastName("Pérez")
                .build();

        Vehicle vehicle = Vehicle.builder()
                .id(30L)
                .user(user)
                .customer(customer)
                .licensePlate("AB123CD")
                .brand("Toyota")
                .model("Corolla")
                .build();

        return Appointment.builder()
                .id(50L)
                .user(user)
                .vehicle(vehicle)
                .scheduledAt(SCHEDULED_AT)
                .status(status)
                .notes("Cambio de aceite")
                .build();
    }
}
