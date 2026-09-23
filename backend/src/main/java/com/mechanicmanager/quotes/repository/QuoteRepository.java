package com.mechanicmanager.quotes.repository;

import com.mechanicmanager.quotes.model.Quote;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface QuoteRepository extends
        JpaRepository<Quote, Long>,
        JpaSpecificationExecutor<Quote> {

    @EntityGraph(attributePaths = {
            "customer",
            "vehicle",
            "items",
            "items.serviceType",
            "items.supply"
    })
    Optional<Quote> findByIdAndUserId(Long id, Long userId);

    Optional<Quote> findTopByUserIdOrderByNumberDesc(Long userId);

    @EntityGraph(attributePaths = {"items", "items.serviceType", "items.supply"})
    Optional<Quote> findByAppointmentIdAndUserId(Long appointmentId, Long userId);
}
