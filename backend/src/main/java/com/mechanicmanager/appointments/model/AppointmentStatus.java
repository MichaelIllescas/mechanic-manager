package com.mechanicmanager.appointments.model;

/**
 * Estados de un turno.
 *
 * <p>{@code SCHEDULED} es el estado inicial y el único desde el que se puede
 * transicionar. {@code COMPLETED} y {@code CANCELLED} son terminales.
 *
 * <p>{@code COMPLETED} significa que el vehículo ingresó al taller: a partir de
 * ahí el estado del trabajo lo lleva la orden de trabajo, no el turno.
 */
public enum AppointmentStatus {
    SCHEDULED,
    COMPLETED,
    CANCELLED
}
