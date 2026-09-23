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
}
