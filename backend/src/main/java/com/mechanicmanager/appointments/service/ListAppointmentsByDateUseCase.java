package com.mechanicmanager.appointments.service;

import com.mechanicmanager.appointments.dto.AppointmentResponse;
import com.mechanicmanager.appointments.mapper.AppointmentMapper;
import com.mechanicmanager.appointments.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ListAppointmentsByDateUseCase {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;

    /**
     * Agenda de un día, ordenada por horario.
     *
     * <p>El rango es semiabierto: desde el inicio del día inclusive hasta el
     * inicio del día siguiente exclusive. Con un extremo cerrado en
     * {@code 23:59:59} un turno agendado dentro del último segundo quedaría
     * fuera de la agenda.
     *
     * <p>El cálculo vive acá y no en el controller para que el criterio sea
     * uno solo: cada llamador que armara el rango por su cuenta podría
     * equivocarse en el límite.
     */
    @Transactional(readOnly = true)
    public List<AppointmentResponse> execute(Long userId, LocalDate date) {
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = date.plusDays(1).atStartOfDay();

        return appointmentRepository
                .findAllByUserIdAndScheduledAtGreaterThanEqualAndScheduledAtLessThanOrderByScheduledAtAsc(
                        userId,
                        from,
                        to
                )
                .stream()
                .map(appointmentMapper::toResponse)
                .toList();
    }
}
