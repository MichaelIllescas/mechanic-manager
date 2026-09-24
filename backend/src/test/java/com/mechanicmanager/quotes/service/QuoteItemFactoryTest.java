package com.mechanicmanager.quotes.service;

import com.mechanicmanager.quotes.dto.QuoteItemRequest;
import com.mechanicmanager.quotes.exception.InvalidQuoteException;
import com.mechanicmanager.servicetypes.repository.ServiceTypeRepository;
import com.mechanicmanager.supplies.repository.SupplyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class QuoteItemFactoryTest {

    @Mock
    private ServiceTypeRepository serviceTypeRepository;
    @Mock
    private SupplyRepository supplyRepository;

    private QuoteItemFactory factory;

    @BeforeEach
    void setUp() {
        factory = new QuoteItemFactory(serviceTypeRepository, supplyRepository);
    }

    @Test
    void acceptsNegativePriceForManualDiscount() {
        QuoteItemRequest request = new QuoteItemRequest(
                null,
                null,
                "Descuento",
                BigDecimal.ONE,
                new BigDecimal("-2500.00")
        );

        var item = factory.create(7L, request);

        assertThat(item.getUnitPrice()).isEqualByComparingTo("-2500.00");
        assertThat(item.getSubtotal()).isEqualByComparingTo("-2500.00");
    }

    @Test
    void rejectsNegativePriceForCatalogItem() {
        QuoteItemRequest request = new QuoteItemRequest(
                5L,
                null,
                "Servicio",
                BigDecimal.ONE,
                BigDecimal.ONE.negate()
        );

        assertThatThrownBy(() -> factory.create(7L, request))
                .isInstanceOf(InvalidQuoteException.class)
                .hasMessage("El precio de un servicio o insumo no puede ser negativo");
    }

    private QuoteItemRequest manual(String description, String unitPrice) {
        return new QuoteItemRequest(null, null, description, BigDecimal.ONE, new BigDecimal(unitPrice));
    }

    @Test
    void acceptsADiscountThatKeepsTheTotalPositive() {
        var items = factory.createAll(7L, java.util.List.of(manual("Mano de obra", "1500"), manual("Descuento", "-500")));

        assertThat(items).hasSize(2);
    }

    @Test
    void acceptsADiscountThatBringsTheTotalToZero() {
        var items = factory.createAll(7L, java.util.List.of(manual("Mano de obra", "1500"), manual("Descuento", "-1500")));

        assertThat(items).hasSize(2);
    }

    @Test
    void rejectsADiscountLargerThanTheTotal() {
        var requests = java.util.List.of(manual("Mano de obra", "1500"), manual("Descuento", "-4500"));

        assertThatThrownBy(() -> factory.createAll(7L, requests))
                .isInstanceOf(InvalidQuoteException.class)
                .hasMessage("El descuento no puede superar el total del presupuesto");
    }
}
