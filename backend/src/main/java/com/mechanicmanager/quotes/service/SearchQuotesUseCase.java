package com.mechanicmanager.quotes.service;

import com.mechanicmanager.quotes.dto.QuotePageResponse;
import com.mechanicmanager.quotes.dto.QuoteSummaryResponse;
import com.mechanicmanager.quotes.exception.InvalidQuoteException;
import com.mechanicmanager.quotes.mapper.QuoteMapper;
import com.mechanicmanager.quotes.model.Quote;
import com.mechanicmanager.quotes.model.QuoteStatus;
import com.mechanicmanager.quotes.repository.QuoteRepository;
import com.mechanicmanager.quotes.repository.QuoteSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchQuotesUseCase {

    private static final int MAX_PAGE_SIZE = 50;

    private final QuoteRepository quoteRepository;
    private final QuoteMapper quoteMapper;

    @Transactional(readOnly = true)
    public QuotePageResponse execute(
            Long userId,
            String search,
            QuoteStatus status,
            Long vehicleId,
            boolean availableForAppointment,
            int page,
            int size
    ) {
        validatePagination(page, size);

        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Quote> result = quoteRepository.findAll(
                QuoteSpecifications.belongsToUserAndMatches(
                        userId,
                        search,
                        status,
                        vehicleId,
                        availableForAppointment
                ),
                pageRequest
        );

        return new QuotePageResponse(
                result.getContent().stream()
                        .map(quoteMapper::toSummaryResponse)
                        .toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new InvalidQuoteException(
                    "El número de página no puede ser negativo"
            );
        }

        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new InvalidQuoteException(
                    "El tamaño de página debe estar entre 1 y " + MAX_PAGE_SIZE
            );
        }
    }
}
