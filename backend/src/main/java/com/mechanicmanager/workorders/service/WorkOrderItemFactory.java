package com.mechanicmanager.workorders.service;

import com.mechanicmanager.servicetypes.model.ServiceType;
import com.mechanicmanager.servicetypes.repository.ServiceTypeRepository;
import com.mechanicmanager.shared.exception.ResourceNotFoundException;
import com.mechanicmanager.supplies.model.Supply;
import com.mechanicmanager.supplies.repository.SupplyRepository;
import com.mechanicmanager.workorders.dto.WorkOrderItemRequest;
import com.mechanicmanager.workorders.exception.InvalidWorkOrderException;
import com.mechanicmanager.workorders.model.WorkOrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class WorkOrderItemFactory {

    private final ServiceTypeRepository serviceTypeRepository;
    private final SupplyRepository supplyRepository;

    /**
     * Crea todos los ítems y verifica que el total no quede negativo: un
     * descuento manual mayor a lo que descuenta no es un importe válido.
     */
    public List<WorkOrderItem> createAll(Long userId, List<WorkOrderItemRequest> requests) {
        List<WorkOrderItem> items = requests.stream()
                .map(request -> create(userId, request))
                .toList();

        BigDecimal total = items.stream()
                .map(WorkOrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (total.signum() < 0) {
            throw new InvalidWorkOrderException(
                    "El descuento no puede superar el total del trabajo realizado");
        }

        return items;
    }

    public WorkOrderItem create(Long userId, WorkOrderItemRequest request) {
        validateSingleSource(request);
        validateCatalogPrice(request);

        ServiceType serviceType = findServiceType(userId, request.serviceTypeId());
        Supply supply = findSupply(userId, request.supplyId());

        return WorkOrderItem.builder()
                .serviceType(serviceType)
                .supply(supply)
                .description(request.description().trim())
                .quantity(request.quantity())
                .unitPrice(request.unitPrice())
                .subtotal(WorkOrderItem.calculateSubtotal(
                        request.quantity(),
                        request.unitPrice()
                ))
                .build();
    }

    private void validateSingleSource(WorkOrderItemRequest request) {
        if (request.serviceTypeId() != null && request.supplyId() != null) {
            throw new InvalidWorkOrderException(
                    "Un ítem no puede ser servicio e insumo al mismo tiempo"
            );
        }
    }

    private void validateCatalogPrice(WorkOrderItemRequest request) {
        boolean catalogItem = request.serviceTypeId() != null || request.supplyId() != null;
        if (catalogItem && request.unitPrice().signum() < 0) {
            throw new InvalidWorkOrderException(
                    "El precio de un servicio o insumo no puede ser negativo"
            );
        }
    }

    private ServiceType findServiceType(Long userId, Long serviceTypeId) {
        if (serviceTypeId == null) {
            return null;
        }

        ServiceType serviceType = serviceTypeRepository
                .findByIdAndUserId(serviceTypeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el servicio indicado"
                ));

        if (!serviceType.isActive()) {
            throw new InvalidWorkOrderException(
                    "El servicio indicado está inactivo"
            );
        }

        return serviceType;
    }

    private Supply findSupply(Long userId, Long supplyId) {
        if (supplyId == null) {
            return null;
        }

        Supply supply = supplyRepository
                .findByIdAndUserId(supplyId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el insumo indicado"
                ));

        if (!supply.isActive()) {
            throw new InvalidWorkOrderException(
                    "El insumo indicado está inactivo"
            );
        }

        return supply;
    }
}
