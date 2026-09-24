package com.mechanicmanager.quotes.service;

import com.mechanicmanager.customers.model.Customer;
import com.mechanicmanager.customers.repository.CustomerRepository;
import com.mechanicmanager.quotes.dto.CreateQuoteRequest;
import com.mechanicmanager.quotes.dto.QuoteResponse;
import com.mechanicmanager.quotes.exception.InvalidQuoteException;
import com.mechanicmanager.quotes.mapper.QuoteMapper;
import com.mechanicmanager.quotes.model.Quote;
import com.mechanicmanager.quotes.model.QuoteItem;
import com.mechanicmanager.quotes.model.QuoteStatus;
import com.mechanicmanager.quotes.repository.QuoteRepository;
import com.mechanicmanager.shared.exception.ResourceNotFoundException;
import com.mechanicmanager.users.model.User;
import com.mechanicmanager.users.repository.UserRepository;
import com.mechanicmanager.vehicles.model.Vehicle;
import com.mechanicmanager.vehicles.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateQuoteUseCase {

    private final QuoteRepository quoteRepository;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final QuoteItemFactory quoteItemFactory;
    private final QuoteMapper quoteMapper;

    @Transactional
    public QuoteResponse execute(Long userId, CreateQuoteRequest request) {
        User user = userRepository.findOneById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el usuario autenticado"
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

        List<QuoteItem> items = quoteItemFactory.createAll(userId, request.items());

        Quote quote = Quote.builder()
                .user(user)
                .customer(customer)
                .vehicle(vehicle)
                .number(nextNumber(userId))
                .validUntil(request.validUntil())
                .status(QuoteStatus.PENDING)
                .total(BigDecimal.ZERO)
                .notes(trimToNull(request.notes()))
                .build();

        quote.replaceItems(items);

        return quoteMapper.toResponse(quoteRepository.save(quote));
    }

    private Long nextNumber(Long userId) {
        return quoteRepository.findTopByUserIdOrderByNumberDesc(userId)
                .map(Quote::getNumber)
                .map(number -> number + 1)
                .orElse(1L);
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
