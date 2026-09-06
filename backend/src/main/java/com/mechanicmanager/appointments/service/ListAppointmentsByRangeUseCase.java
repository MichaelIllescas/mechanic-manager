package com.mechanicmanager.appointments.service;

import com.mechanicmanager.appointments.dto.AppointmentResponse;
import com.mechanicmanager.appointments.exception.InvalidDateRangeException;
import com.mechanicmanager.appointments.mapper.AppointmentMapper;
import com.mechanicmanager.appointments.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ListAppointmentsByRangeUseCase {

    /**
     * Un trimestre alcanza para cualquier vista de la agenda: el mes es la más
     * amplia y necesita seis semanas. El tope evita que una consulta de años
     * traiga todos los turnos del taller en una sola respuesta.
     */
    static final int MAX_RANGE_DAYS = 92;

    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;

    /**
     * Turnos entre dos fechas, ordenados por horario.
     *
     * <p>Sirve a las tres vistas de la agenda: el día es un rango de un día, la
     * semana de siete y el mes la grilla completa, que arranca antes del día 1
     * y termina después del último para llenar la primera y la última semana.
     *
     * <p>{@code from} y {@code to} son inclusivos para quien llama: "del 1 al
     * 7" incluye el 7. Contra la base el rango es semiabierto, hasta el inicio
     * del día siguiente a {@code to}: con un extremo cerrado en 23:59:59 un
     * turno agendado dentro del último segundo quedaría afuera.
     */
    @Transactional(readOnly = true)
    public List<AppointmentResponse> execute(Long userId, LocalDate from, LocalDate to) {
        if (to.isBefore(from)) {
            throw new InvalidDateRangeException(
                    "La fecha de inicio no puede ser posterior a la de fin");
        }

        if (ChronoUnit.DAYS.between(from, to) + 1 > MAX_RANGE_DAYS) {
            throw new InvalidDateRangeException(
                    "El rango no puede superar los " + MAX_RANGE_DAYS + " días");
        }

        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay();

        return appointmentRepository
                .findAllByUserIdAndScheduledAtGreaterThanEqualAndScheduledAtLessThanOrderByScheduledAtAsc(
                        userId,
                        start,
                        end
                )
                .stream()
                .map(appointmentMapper::toResponse)
                .toList();
    }
}
