package com.mechanicmanager.quotes.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class QuoteValidityValidationTest {

    private static final LocalDate EXPIRED = LocalDate.now().minusDays(10);

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private final List<QuoteItemRequest> items = List.of(
            new QuoteItemRequest(null, null, "Mano de obra", BigDecimal.ONE, BigDecimal.TEN));

    @Test
    void creatingRejectsAValidityDateInThePast() {
        var request = new CreateQuoteRequest(1L, 2L, EXPIRED, null, items);

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("validUntil");
    }

    @Test
    void editingAcceptsAnExpiredQuoteWithoutChangingItsDate() {
        var request = new UpdateQuoteRequest(1L, 2L, EXPIRED, "Nota corregida", items);

        assertThat(validator.validate(request)).isEmpty();
    }
}
