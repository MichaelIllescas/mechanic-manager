package com.mechanicmanager.quotes.repository;

import com.mechanicmanager.quotes.model.Quote;
import com.mechanicmanager.quotes.model.QuoteStatus;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
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
            QuoteStatus status
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

            if (search != null && !search.isBlank()) {
                String normalizedSearch = search.trim().toLowerCase(Locale.ROOT);
                String pattern = "%" + normalizedSearch + "%";

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
                        pattern
                ));
                matches.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("vehicle").get("licensePlate")),
                        pattern
                ));

                parseNumber(normalizedSearch).ifPresent(number ->
                        matches.add(criteriaBuilder.equal(root.get("number"), number))
                );

                filters.add(criteriaBuilder.or(matches.toArray(Predicate[]::new)));
            }

            return criteriaBuilder.and(filters.toArray(Predicate[]::new));
        };
    }

    private static java.util.Optional<Long> parseNumber(String search) {
        try {
            return java.util.Optional.of(Long.parseLong(search));
        } catch (NumberFormatException exception) {
            return java.util.Optional.empty();
        }
    }
}
