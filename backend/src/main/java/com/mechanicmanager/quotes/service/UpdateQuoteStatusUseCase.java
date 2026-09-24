package com.mechanicmanager.quotes.service;

import com.mechanicmanager.quotes.dto.QuoteResponse;
import com.mechanicmanager.quotes.dto.UpdateQuoteStatusRequest;
import com.mechanicmanager.quotes.mapper.QuoteMapper;
import com.mechanicmanager.quotes.model.Quote;
import com.mechanicmanager.quotes.repository.QuoteRepository;
import com.mechanicmanager.quotes.exception.QuoteHasWorkOrderException;
import com.mechanicmanager.shared.exception.ResourceNotFoundException;
import com.mechanicmanager.workorders.repository.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateQuoteStatusUseCase {

    private final QuoteRepository quoteRepository;
    private final QuoteMapper quoteMapper;
    private final WorkOrderRepository workOrderRepository;

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

        // Con un trabajo ya realizado, el presupuesto quedó respaldado por él:
        // cambiar su estado dejaría al historial contradiciéndose.
        boolean hasWorkOrder = workOrderRepository.existsByQuoteIdAndUserId(quoteId, userId);
        if (hasWorkOrder && quote.getStatus() != request.status()) {
            throw new QuoteHasWorkOrderException();
        }

        quote.setStatus(request.status());

        return quoteMapper.toResponse(quoteRepository.save(quote), hasWorkOrder);
    }
}
