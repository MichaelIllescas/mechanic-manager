package com.mechanicmanager.quotes.service;

import com.mechanicmanager.customers.model.Customer;
import com.mechanicmanager.quotes.dto.UpdateQuoteStatusRequest;
import com.mechanicmanager.quotes.exception.QuoteHasWorkOrderException;
import com.mechanicmanager.quotes.mapper.QuoteMapper;
import com.mechanicmanager.quotes.model.Quote;
import com.mechanicmanager.quotes.model.QuoteStatus;
import com.mechanicmanager.quotes.repository.QuoteRepository;
import com.mechanicmanager.vehicles.model.Vehicle;
import com.mechanicmanager.workorders.repository.WorkOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateQuoteStatusUseCaseTest {

    private static final Long USER_ID = 7L;
    private static final Long QUOTE_ID = 21L;

    @Mock
    private QuoteRepository quoteRepository;

    @Mock
    private WorkOrderRepository workOrderRepository;

    private UpdateQuoteStatusUseCase useCase;
    private Quote quote;

    @BeforeEach
    void setUp() {
        useCase = new UpdateQuoteStatusUseCase(quoteRepository, new QuoteMapper(), workOrderRepository);
        quote = Quote.builder()
                .id(QUOTE_ID)
                .number(1L)
                .status(QuoteStatus.APPROVED)
                .customer(Customer.builder().id(1L).firstName("Ana").lastName("Gómez").build())
                .vehicle(Vehicle.builder().id(2L).licensePlate("AB123CD").brand("VW").model("Gol").build())
                .build();
        when(quoteRepository.findByIdAndUserId(QUOTE_ID, USER_ID)).thenReturn(Optional.of(quote));
    }

    private UpdateQuoteStatusRequest request(QuoteStatus status) {
        return new UpdateQuoteStatusRequest(status);
    }

    @Test
    void changesTheStatusWhenThereIsNoWorkOrder() {
        when(workOrderRepository.existsByQuoteIdAndUserId(QUOTE_ID, USER_ID)).thenReturn(false);
        when(quoteRepository.save(any(Quote.class))).thenAnswer(call -> call.getArgument(0));

        var response = useCase.execute(USER_ID, QUOTE_ID, request(QuoteStatus.REJECTED));

        assertThat(response.status()).isEqualTo(QuoteStatus.REJECTED);
        assertThat(response.hasWorkOrder()).isFalse();
    }

    @Test
    void rejectsChangingTheStatusOfAQuoteWithAWorkOrder() {
        when(workOrderRepository.existsByQuoteIdAndUserId(QUOTE_ID, USER_ID)).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(USER_ID, QUOTE_ID, request(QuoteStatus.PENDING)))
                .isInstanceOf(QuoteHasWorkOrderException.class);

        assertThat(quote.getStatus()).isEqualTo(QuoteStatus.APPROVED);
        verify(quoteRepository, never()).save(any(Quote.class));
    }

    @Test
    void allowsRepeatingTheCurrentStatusEvenWithAWorkOrder() {
        when(workOrderRepository.existsByQuoteIdAndUserId(QUOTE_ID, USER_ID)).thenReturn(true);
        when(quoteRepository.save(any(Quote.class))).thenAnswer(call -> call.getArgument(0));

        var response = useCase.execute(USER_ID, QUOTE_ID, request(QuoteStatus.APPROVED));

        assertThat(response.status()).isEqualTo(QuoteStatus.APPROVED);
        assertThat(response.hasWorkOrder()).isTrue();
    }
}
