package com.mechanicmanager.workorders.repository;

import com.mechanicmanager.workorders.model.WorkOrder;
import com.mechanicmanager.workorders.model.WorkOrderStatus;
import com.mechanicmanager.shared.search.LikePattern;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class WorkOrderSpecifications {

    private WorkOrderSpecifications() {
    }

    public static Specification<WorkOrder> belongsToUserAndMatches(
            Long userId,
            String search,
            WorkOrderStatus status,
            Long vehicleId,
            LocalDate from,
            LocalDate to
    ) {
        return (root, query, criteriaBuilder) -> {
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

            if (from != null) {
                filters.add(criteriaBuilder.greaterThanOrEqualTo(root.get("performedAt"), from));
            }

            if (to != null) {
                filters.add(criteriaBuilder.lessThanOrEqualTo(root.get("performedAt"), to));
            }

            if (search != null && !search.isBlank()) {
                String normalizedSearch = search.trim().toLowerCase(Locale.ROOT);
                String pattern = LikePattern.contains(normalizedSearch);

                Expression<String> customerFullName = criteriaBuilder.concat(
                        criteriaBuilder.concat(
                                root.get("vehicle").get("customer").get("firstName"),
                                " "
                        ),
                        root.get("vehicle").get("customer").get("lastName")
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

                filters.add(criteriaBuilder.or(matches.toArray(Predicate[]::new)));
            }

            return criteriaBuilder.and(filters.toArray(Predicate[]::new));
        };
    }
}
