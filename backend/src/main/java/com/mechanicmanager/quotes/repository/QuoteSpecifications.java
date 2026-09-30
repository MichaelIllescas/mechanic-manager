package com.mechanicmanager.quotes.repository;

import com.mechanicmanager.appointments.model.Appointment;
import com.mechanicmanager.appointments.model.AppointmentStatus;
import com.mechanicmanager.quotes.model.Quote;
import com.mechanicmanager.quotes.model.QuoteStatus;
import com.mechanicmanager.shared.search.LikePattern;
import com.mechanicmanager.workorders.model.WorkOrder;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class QuoteSpecifications {

    private QuoteSpecifications() {
    }

    public static Specification<Quote> belongsToUserAndMatches(
            Long userId,
            String search,
            QuoteStatus status,
            Long vehicleId,
            boolean availableForAppointment
    ) {
        return (root, query, criteriaBuilder) -> {
            if (!isCountQuery(query.getResultType())) {
                root.fetch("customer");
                root.fetch("vehicle");
            }

            List<Predicate> filters = new ArrayList<>();

            filters.add(criteriaBuilder.equal(
                    root.get("user").get("id"),
                    userId
            ));

            if (status != null) {
                filters.add(criteriaBuilder.equal(root.get("status"), status));
            }

            if (vehicleId != null) {
                filters.add(criteriaBuilder.equal(root.get("vehicle").get("id"), vehicleId));
            }

            if (availableForAppointment) {
                filters.add(isAvailableForAppointment(root, query, criteriaBuilder));
            }

            if (search != null && !search.isBlank()) {
                String normalizedSearch = search.trim().toLowerCase(Locale.ROOT);
                String pattern = LikePattern.contains(normalizedSearch);

                Expression<String> customerFullName = criteriaBuilder.concat(
                        criteriaBuilder.concat(
                                root.get("customer").get("firstName"),
                                " "
                        ),
                        root.get("customer").get("lastName")
                );

                List<Predicate> matches = new ArrayList<>();
                matches.add(criteriaBuilder.like(
                        criteriaBuilder.lower(customerFullName),
                        pattern, LikePattern.ESCAPE
                ));
                matches.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("vehicle").get("licensePlate")),
                        pattern, LikePattern.ESCAPE
                ));

                parseNumber(normalizedSearch).ifPresent(number ->
                        matches.add(criteriaBuilder.equal(root.get("number"), number))
                );

                filters.add(criteriaBuilder.or(matches.toArray(Predicate[]::new)));
            }

            return criteriaBuilder.and(filters.toArray(Predicate[]::new));
        };
    }

    /**
     * Misma regla que valida el alta de turnos: aprobado, sin trabajo realizado
     * y sin un turno activo que lo use (los cancelados no cuentan).
     */
    private static Predicate isAvailableForAppointment(
            Root<Quote> root,
            CriteriaQuery<?> query,
            CriteriaBuilder criteriaBuilder
    ) {
        Subquery<Long> workOrders = query.subquery(Long.class);
        Root<WorkOrder> workOrder = workOrders.from(WorkOrder.class);
        workOrders.select(workOrder.get("id"))
                .where(criteriaBuilder.equal(workOrder.get("quote"), root));

        Subquery<Long> activeAppointments = query.subquery(Long.class);
        Root<Appointment> appointment = activeAppointments.from(Appointment.class);
        activeAppointments.select(appointment.get("id"))
                .where(
                        criteriaBuilder.equal(appointment.get("quote"), root),
                        criteriaBuilder.notEqual(
                                appointment.get("status"),
                                AppointmentStatus.CANCELLED
                        )
                );

        return criteriaBuilder.and(
                criteriaBuilder.equal(root.get("status"), QuoteStatus.APPROVED),
                criteriaBuilder.not(criteriaBuilder.exists(workOrders)),
                criteriaBuilder.not(criteriaBuilder.exists(activeAppointments))
        );
    }

    private static boolean isCountQuery(Class<?> resultType) {
        return Long.class.equals(resultType) || long.class.equals(resultType);
    }

    private static java.util.Optional<Long> parseNumber(String search) {
        try {
            return java.util.Optional.of(Long.parseLong(search));
        } catch (NumberFormatException exception) {
            return java.util.Optional.empty();
        }
    }
}
