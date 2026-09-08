package com.mechanicmanager.quotes.service;

import com.mechanicmanager.quotes.dto.QuoteResponse;
import com.mechanicmanager.quotes.dto.UpdateQuoteStatusRequest;
import com.mechanicmanager.quotes.mapper.QuoteMapper;
import com.mechanicmanager.quotes.model.Quote;
import com.mechanicmanager.quotes.repository.QuoteRepository;
import com.mechanicmanager.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateQuoteStatusUseCase {

    private final QuoteRepository quoteRepository;
    private final QuoteMapper quoteMapper;

    @Transactional
    public QuoteResponse execute(
            Long userId,
            Long quoteId,
            UpdateQuoteStatusRequest request
    ) {
        Quote quote = quoteRepository.findByIdAndUserId(quoteId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el presupuesto indicado"
                ));

        quote.setStatus(request.status());

        return quoteMapper.toResponse(quoteRepository.save(quote));
    }
}
