package com.mechanicmanager.quotes.service;

import com.mechanicmanager.customers.model.Customer;
import com.mechanicmanager.customers.repository.CustomerRepository;
import com.mechanicmanager.quotes.dto.QuoteResponse;
import com.mechanicmanager.quotes.dto.UpdateQuoteRequest;
import com.mechanicmanager.quotes.exception.InvalidQuoteException;
import com.mechanicmanager.quotes.mapper.QuoteMapper;
import com.mechanicmanager.quotes.model.Quote;
import com.mechanicmanager.quotes.model.QuoteItem;
import com.mechanicmanager.quotes.repository.QuoteRepository;
import com.mechanicmanager.shared.exception.ResourceNotFoundException;
import com.mechanicmanager.vehicles.model.Vehicle;
import com.mechanicmanager.vehicles.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UpdateQuoteUseCase {

    private final QuoteRepository quoteRepository;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final QuoteItemFactory quoteItemFactory;
    private final QuoteMapper quoteMapper;

    @Transactional
    public QuoteResponse execute(
            Long userId,
            Long quoteId,
            UpdateQuoteRequest request
    ) {
        Quote quote = quoteRepository.findByIdAndUserId(quoteId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el presupuesto indicado"
                ));

        Customer customer = customerRepository
                .findByIdAndUserId(request.customerId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el cliente indicado"
                ));

        Vehicle vehicle = vehicleRepository
                .findByIdAndUserId(request.vehicleId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el vehículo indicado"
                ));

        validateVehicleCustomer(vehicle, customer);

        List<QuoteItem> items = request.items().stream()
                .map(item -> quoteItemFactory.create(userId, item))
                .toList();

        quote.setCustomer(customer);
        quote.setVehicle(vehicle);
        quote.setValidUntil(request.validUntil());
        quote.setNotes(trimToNull(request.notes()));
        quote.replaceItems(items);

        return quoteMapper.toResponse(quoteRepository.save(quote));
    }

    private void validateVehicleCustomer(Vehicle vehicle, Customer customer) {
        if (!vehicle.getCustomer().getId().equals(customer.getId())) {
            throw new InvalidQuoteException(
                    "El vehículo indicado no pertenece al cliente seleccionado"
            );
        }
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
