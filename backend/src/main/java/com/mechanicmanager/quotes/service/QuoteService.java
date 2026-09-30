package com.mechanicmanager.quotes.service;

import com.mechanicmanager.quotes.dto.CreateQuoteRequest;
import com.mechanicmanager.quotes.dto.QuotePageResponse;
import com.mechanicmanager.quotes.dto.QuoteResponse;
import com.mechanicmanager.quotes.dto.UpdateQuoteRequest;
import com.mechanicmanager.quotes.dto.UpdateQuoteStatusRequest;
import com.mechanicmanager.quotes.model.QuoteStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuoteService {

    private final CreateQuoteUseCase createQuoteUseCase;
    private final GetQuoteUseCase getQuoteUseCase;
    private final SearchQuotesUseCase searchQuotesUseCase;
    private final UpdateQuoteUseCase updateQuoteUseCase;
    private final UpdateQuoteStatusUseCase updateQuoteStatusUseCase;
    private final DeleteQuoteUseCase deleteQuoteUseCase;

    public QuoteResponse create(Long userId, CreateQuoteRequest request) {
        return createQuoteUseCase.execute(userId, request);
    }

    public QuoteResponse findById(Long userId, Long quoteId) {
        return getQuoteUseCase.execute(userId, quoteId);
    }

    public QuotePageResponse search(
            Long userId,
            String search,
            QuoteStatus status,
            Long vehicleId,
            boolean availableForAppointment,
            int page,
            int size
    ) {
        return searchQuotesUseCase.execute(
                userId,
                search,
                status,
                vehicleId,
                availableForAppointment,
                page,
                size
        );
    }

    public QuoteResponse update(
            Long userId,
            Long quoteId,
            UpdateQuoteRequest request
    ) {
        return updateQuoteUseCase.execute(userId, quoteId, request);
    }

    public QuoteResponse updateStatus(
            Long userId,
            Long quoteId,
            UpdateQuoteStatusRequest request
    ) {
        return updateQuoteStatusUseCase.execute(userId, quoteId, request);
    }

    public void delete(Long userId, Long quoteId) {
        deleteQuoteUseCase.execute(userId, quoteId);
    }
}
