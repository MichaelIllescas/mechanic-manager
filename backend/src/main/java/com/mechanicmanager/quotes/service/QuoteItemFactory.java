package com.mechanicmanager.quotes.service;

import com.mechanicmanager.quotes.dto.QuoteItemRequest;
import com.mechanicmanager.quotes.exception.InvalidQuoteException;
import com.mechanicmanager.quotes.model.QuoteItem;
import com.mechanicmanager.servicetypes.model.ServiceType;
import com.mechanicmanager.servicetypes.repository.ServiceTypeRepository;
import com.mechanicmanager.shared.exception.ResourceNotFoundException;
import com.mechanicmanager.supplies.model.Supply;
import com.mechanicmanager.supplies.repository.SupplyRepository;
import com.mechanicmanager.shared.validation.MoneyLimits;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class QuoteItemFactory {

    private final ServiceTypeRepository serviceTypeRepository;
    private final SupplyRepository supplyRepository;

    /**
     * Crea todos los ítems y verifica que los importes sean válidos: que quepan
     * en las columnas y que un descuento manual no deje el total negativo.
     */
    public List<QuoteItem> createAll(Long userId, List<QuoteItemRequest> requests) {
        List<QuoteItem> items = requests.stream()
                .map(request -> create(userId, request))
                .toList();

        if (items.stream().map(QuoteItem::getSubtotal).anyMatch(MoneyLimits::exceedsMaxAmount)) {
            throw new InvalidQuoteException("El importe de un ítem supera el máximo permitido");
        }

        BigDecimal total = items.stream()
                .map(QuoteItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (MoneyLimits.exceedsMaxAmount(total)) {
            throw new InvalidQuoteException("El total supera el importe máximo permitido");
        }

        if (total.signum() < 0) {
            throw new InvalidQuoteException("El descuento no puede superar el total del presupuesto");
        }

        return items;
    }

    public QuoteItem create(Long userId, QuoteItemRequest request) {
        validateSingleSource(request);
        validateCatalogPrice(request);

        ServiceType serviceType = findServiceType(userId, request.serviceTypeId());
        Supply supply = findSupply(userId, request.supplyId());

        return QuoteItem.builder()
                .serviceType(serviceType)
                .supply(supply)
                .description(request.description().trim())
                .quantity(request.quantity())
                .unitPrice(request.unitPrice())
                .subtotal(QuoteItem.calculateSubtotal(
                        request.quantity(),
                        request.unitPrice()
                ))
                .build();
    }

    private void validateSingleSource(QuoteItemRequest request) {
        if (request.serviceTypeId() != null && request.supplyId() != null) {
            throw new InvalidQuoteException(
                    "Un ítem no puede ser servicio e insumo al mismo tiempo"
            );
        }
    }

    private void validateCatalogPrice(QuoteItemRequest request) {
        boolean catalogItem = request.serviceTypeId() != null || request.supplyId() != null;
        if (catalogItem && request.unitPrice().signum() < 0) {
            throw new InvalidQuoteException(
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
            throw new InvalidQuoteException(
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
            throw new InvalidQuoteException(
                    "El insumo indicado está inactivo"
            );
        }

        return supply;
    }
}
