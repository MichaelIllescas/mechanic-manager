package com.mechanicmanager.shared.validation;

import com.mechanicmanager.servicetypes.dto.CreateServiceTypeRequest;
import com.mechanicmanager.servicetypes.dto.UpdateServiceTypeRequest;
import com.mechanicmanager.supplies.dto.CreateSupplyRequest;
import com.mechanicmanager.supplies.dto.UpdateSupplyRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogPriceLimitTest {

    private static final BigDecimal FITS = new BigDecimal("9999999999.99");
    private static final BigDecimal TOO_BIG = new BigDecimal("10000000000.00");
    private static final BigDecimal TOO_MANY_DECIMALS = new BigDecimal("10.999");

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private static Set<String> fields(Set<? extends ConstraintViolation<?>> violations) {
        return violations.stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    @Test
    void acceptsTheLargestPriceThatFitsTheColumn() {
        assertThat(validator.validate(new CreateServiceTypeRequest("Alineación", null, FITS))).isEmpty();
        assertThat(validator.validate(new CreateSupplyRequest("Filtro", FITS))).isEmpty();
    }

    @Test
    void rejectsAServicePriceThatDoesNotFit() {
        assertThat(fields(validator.validate(new CreateServiceTypeRequest("Alineación", null, TOO_BIG))))
                .containsExactly("basePrice");
        assertThat(fields(validator.validate(new UpdateServiceTypeRequest("Alineación", null, TOO_BIG))))
                .containsExactly("basePrice");
    }

    @Test
    void rejectsASupplyPriceThatDoesNotFit() {
        assertThat(fields(validator.validate(new CreateSupplyRequest("Filtro", TOO_BIG))))
                .containsExactly("price");
        assertThat(fields(validator.validate(new UpdateSupplyRequest("Filtro", TOO_BIG))))
                .containsExactly("price");
    }

    @Test
    void rejectsMoreThanTwoDecimals() {
        assertThat(fields(validator.validate(new CreateSupplyRequest("Filtro", TOO_MANY_DECIMALS))))
                .containsExactly("price");
    }
}
