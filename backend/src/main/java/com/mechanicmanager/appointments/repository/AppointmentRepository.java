package com.mechanicmanager.appointments.repository;

import com.mechanicmanager.appointments.model.Appointment;
import com.mechanicmanager.appointments.model.AppointmentStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    @EntityGraph(attributePaths = {"vehicle", "vehicle.customer", "quote"})
    List<Appointment> findAllByUserIdAndScheduledAtGreaterThanEqualAndScheduledAtLessThanOrderByScheduledAtAsc(
            Long userId,
            LocalDateTime from,
            LocalDateTime to
    );

    long countByUserIdAndScheduledAtGreaterThanEqualAndScheduledAtLessThan(
            Long userId,
            LocalDateTime from,
            LocalDateTime to
    );

    @EntityGraph(attributePaths = {"vehicle", "vehicle.customer"})
    List<Appointment> findTop6ByUserIdAndScheduledAtGreaterThanEqualAndScheduledAtLessThanOrderByScheduledAtAsc(
            Long userId,
            LocalDateTime from,
            LocalDateTime to
    );

    @EntityGraph(attributePaths = {"vehicle", "vehicle.customer", "quote"})
    Optional<Appointment> findByIdAndUserId(Long id, Long userId);

    /**
     * Un presupuesto está tomado mientras algún turno no cancelado lo use; los
     * cancelados lo conservan solo como historial.
     */
    boolean existsByQuoteIdAndUserIdAndStatusNot(Long quoteId, Long userId, AppointmentStatus status);

    List<Appointment> findAllByQuoteIdAndUserId(Long quoteId, Long userId);

}
