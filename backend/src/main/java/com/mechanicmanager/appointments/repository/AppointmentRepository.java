package com.mechanicmanager.appointments.repository;

import com.mechanicmanager.appointments.model.Appointment;
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

}
