package com.mechanicmanager.quotes.mapper;

import com.mechanicmanager.quotes.dto.QuoteItemResponse;
import com.mechanicmanager.quotes.dto.QuoteResponse;
import com.mechanicmanager.quotes.dto.QuoteSummaryResponse;
import com.mechanicmanager.quotes.model.Quote;
import com.mechanicmanager.quotes.model.QuoteItem;
import org.springframework.stereotype.Component;

@Component
public class QuoteMapper {

    public QuoteResponse toResponse(Quote quote) {
        return new QuoteResponse(
                quote.getId(),
                quote.getNumber(),
                quote.getCustomer().getId(),
                customerName(quote),
                quote.getVehicle().getId(),
                quote.getVehicle().getLicensePlate(),
                vehicleDescription(quote),
                quote.getValidUntil(),
                quote.getStatus(),
                quote.getTotal(),
                quote.getNotes(),
                quote.getItems().stream()
                        .map(this::toItemResponse)
                        .toList(),
                quote.getCreatedAt(),
                quote.getUpdatedAt()
        );
    }

    public QuoteSummaryResponse toSummaryResponse(Quote quote) {
        return new QuoteSummaryResponse(
                quote.getId(),
                quote.getNumber(),
                quote.getCustomer().getId(),
                customerName(quote),
                quote.getVehicle().getId(),
                quote.getVehicle().getLicensePlate(),
                quote.getValidUntil(),
                quote.getStatus(),
                quote.getTotal(),
                quote.getCreatedAt()
        );
    }

    private QuoteItemResponse toItemResponse(QuoteItem item) {
        return new QuoteItemResponse(
                item.getId(),
                item.getServiceType() == null
                        ? null
                        : item.getServiceType().getId(),
                item.getSupply() == null
                        ? null
                        : item.getSupply().getId(),
                item.getDescription(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal()
        );
    }

    private String customerName(Quote quote) {
        return quote.getCustomer().getFirstName()
                + " "
                + quote.getCustomer().getLastName();
    }

    private String vehicleDescription(Quote quote) {
        return quote.getVehicle().getBrand()
                + " "
                + quote.getVehicle().getModel();
    }
}
