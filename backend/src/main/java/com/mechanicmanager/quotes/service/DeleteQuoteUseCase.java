package com.mechanicmanager.quotes.service;

import com.mechanicmanager.appointments.repository.AppointmentRepository;
import com.mechanicmanager.quotes.model.Quote;
import com.mechanicmanager.quotes.repository.QuoteRepository;
import com.mechanicmanager.shared.exception.ResourceNotFoundException;
import com.mechanicmanager.workorders.repository.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteQuoteUseCase {

    private final QuoteRepository quoteRepository;
    private final WorkOrderRepository workOrderRepository;
    private final AppointmentRepository appointmentRepository;

    @Transactional
    public void execute(Long userId, Long quoteId) {
        Quote quote = quoteRepository.findByIdAndUserId(quoteId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el presupuesto indicado"));

        workOrderRepository.findByQuoteIdAndUserId(quoteId, userId)
                .ifPresent(workOrder -> workOrder.setQuote(null));
        appointmentRepository.findAllByQuoteIdAndUserId(quoteId, userId)
                .forEach(appointment -> appointment.setQuote(null));

        quoteRepository.flush();
        quoteRepository.delete(quote);
    }
}
