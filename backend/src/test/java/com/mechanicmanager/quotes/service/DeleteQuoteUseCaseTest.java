package com.mechanicmanager.quotes.service;

import com.mechanicmanager.appointments.model.Appointment;
import com.mechanicmanager.quotes.model.Quote;
import com.mechanicmanager.quotes.repository.QuoteRepository;
import com.mechanicmanager.shared.exception.ResourceNotFoundException;
import com.mechanicmanager.workorders.model.WorkOrder;
import com.mechanicmanager.workorders.repository.WorkOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteQuoteUseCaseTest {

    @Mock
    private QuoteRepository quoteRepository;
    @Mock
    private WorkOrderRepository workOrderRepository;

    private DeleteQuoteUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteQuoteUseCase(quoteRepository, workOrderRepository);
    }

    @Test
    void unlinksAppointmentAndWorkOrderBeforeDeletingQuote() {
        Appointment appointment = Appointment.builder().id(50L).build();
        Quote quote = Quote.builder().id(15L).appointment(appointment).build();
        WorkOrder workOrder = WorkOrder.builder().quote(quote).build();

        when(quoteRepository.findByIdAndUserId(15L, 7L)).thenReturn(Optional.of(quote));
        when(workOrderRepository.findByQuoteIdAndUserId(15L, 7L))
                .thenReturn(Optional.of(workOrder));

        useCase.execute(7L, 15L);

        assertThat(quote.getAppointment()).isNull();
        assertThat(workOrder.getQuote()).isNull();
        InOrder order = inOrder(quoteRepository);
        order.verify(quoteRepository).flush();
        order.verify(quoteRepository).delete(quote);
    }

    @Test
    void rejectsQuoteFromAnotherUser() {
        when(quoteRepository.findByIdAndUserId(15L, 7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(7L, 15L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No se encontró el presupuesto indicado");

        verify(workOrderRepository, never()).findByQuoteIdAndUserId(15L, 7L);
        verify(quoteRepository, never()).delete(any(Quote.class));
    }
}
