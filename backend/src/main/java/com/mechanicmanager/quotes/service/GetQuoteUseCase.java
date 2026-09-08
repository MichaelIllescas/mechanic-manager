package com.mechanicmanager.quotes.service;

import com.mechanicmanager.quotes.dto.QuoteResponse;
import com.mechanicmanager.quotes.mapper.QuoteMapper;
import com.mechanicmanager.quotes.model.Quote;
import com.mechanicmanager.quotes.repository.QuoteRepository;
import com.mechanicmanager.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetQuoteUseCase {

    private final QuoteRepository quoteRepository;
    private final QuoteMapper quoteMapper;

    @Transactional(readOnly = true)
    public QuoteResponse execute(Long userId, Long quoteId) {
        Quote quote = quoteRepository.findByIdAndUserId(quoteId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el presupuesto indicado"
                ));

        return quoteMapper.toResponse(quote);
    }
}
