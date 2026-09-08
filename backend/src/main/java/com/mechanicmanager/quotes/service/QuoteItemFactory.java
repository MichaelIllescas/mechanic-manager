package com.mechanicmanager.quotes.service;

import com.mechanicmanager.quotes.dto.QuoteItemRequest;
import com.mechanicmanager.quotes.exception.InvalidQuoteException;
import com.mechanicmanager.quotes.model.QuoteItem;
import com.mechanicmanager.servicetypes.model.ServiceType;
import com.mechanicmanager.servicetypes.repository.ServiceTypeRepository;
import com.mechanicmanager.shared.exception.ResourceNotFoundException;
import com.mechanicmanager.supplies.model.Supply;
import com.mechanicmanager.supplies.repository.SupplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuoteItemFactory {

    private final ServiceTypeRepository serviceTypeRepository;
    private final SupplyRepository supplyRepository;

    public QuoteItem create(Long userId, QuoteItemRequest request) {
        validateSingleSource(request);

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
