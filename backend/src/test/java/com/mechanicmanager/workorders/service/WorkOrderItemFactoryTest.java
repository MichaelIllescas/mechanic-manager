package com.mechanicmanager.workorders.service;

import com.mechanicmanager.servicetypes.repository.ServiceTypeRepository;
import com.mechanicmanager.supplies.repository.SupplyRepository;
import com.mechanicmanager.workorders.dto.WorkOrderItemRequest;
import com.mechanicmanager.workorders.exception.InvalidWorkOrderException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class WorkOrderItemFactoryTest {

    @Mock
    private ServiceTypeRepository serviceTypeRepository;
    @Mock
    private SupplyRepository supplyRepository;

    private WorkOrderItemFactory factory;

    @BeforeEach
    void setUp() {
        factory = new WorkOrderItemFactory(serviceTypeRepository, supplyRepository);
    }

    @Test
    void acceptsNegativePriceForManualDiscount() {
        WorkOrderItemRequest request = new WorkOrderItemRequest(
                null, null, "Descuento", BigDecimal.ONE, new BigDecimal("-3000.00")
        );

        var item = factory.create(7L, request);

        assertThat(item.getSubtotal()).isEqualByComparingTo("-3000.00");
    }

    @Test
    void rejectsNegativePriceForCatalogItem() {
        WorkOrderItemRequest request = new WorkOrderItemRequest(
                5L, null, "Servicio", BigDecimal.ONE, BigDecimal.ONE.negate()
        );

        assertThatThrownBy(() -> factory.create(7L, request))
                .isInstanceOf(InvalidWorkOrderException.class)
                .hasMessage("El precio de un servicio o insumo no puede ser negativo");
    }

    private WorkOrderItemRequest manual(String description, String unitPrice) {
        return new WorkOrderItemRequest(null, null, description, BigDecimal.ONE, new BigDecimal(unitPrice));
    }

    @Test
    void acceptsADiscountThatKeepsTheTotalPositive() {
        var items = factory.createAll(7L, java.util.List.of(manual("Mano de obra", "1500"), manual("Descuento", "-500")));

        assertThat(items).hasSize(2);
    }

    private WorkOrderItemRequest big(String quantity, String unitPrice) {
        return new WorkOrderItemRequest(null, null, "Item", new BigDecimal(quantity), new BigDecimal(unitPrice));
    }

    @Test
    void rejectsAnItemWhoseSubtotalDoesNotFitTheColumn() {
        var requests = java.util.List.of(big("999999999.999", "9999999999.99"));

        assertThatThrownBy(() -> factory.createAll(7L, requests))
                .isInstanceOf(InvalidWorkOrderException.class)
                .hasMessage("El importe de un ítem supera el máximo permitido");
    }

    @Test
    void rejectsATotalThatDoesNotFitTheColumn() {
        var requests = java.util.List.of(big("60", "9000000000"), big("60", "9000000000"));

        assertThatThrownBy(() -> factory.createAll(7L, requests))
                .isInstanceOf(InvalidWorkOrderException.class)
                .hasMessage("El total supera el importe máximo permitido");
    }

    @Test
    void rejectsADiscountLargerThanTheTotal() {
        var requests = java.util.List.of(manual("Mano de obra", "1500"), manual("Descuento", "-4500"));

        assertThatThrownBy(() -> factory.createAll(7L, requests))
                .isInstanceOf(InvalidWorkOrderException.class)
                .hasMessage("El descuento no puede superar el total del trabajo realizado");
    }
}
