package com.mechanicmanager.appointments.service;

import com.mechanicmanager.appointments.model.Appointment;
import com.mechanicmanager.appointments.model.AppointmentStatus;
import com.mechanicmanager.appointments.repository.AppointmentRepository;
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

import static com.mechanicmanager.appointments.service.AppointmentFixtures.appointmentWith;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteAppointmentUseCaseTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private QuoteRepository quoteRepository;
    @Mock
    private WorkOrderRepository workOrderRepository;

    private DeleteAppointmentUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteAppointmentUseCase(
                appointmentRepository,
                quoteRepository,
                workOrderRepository
        );
    }

    @Test
    void unlinksQuoteAndWorkOrderBeforeDeletingAppointment() {
        Appointment appointment = appointmentWith(AppointmentStatus.SCHEDULED);
        Quote quote = Quote.builder().appointment(appointment).build();
        WorkOrder workOrder = WorkOrder.builder().appointment(appointment).build();

        when(appointmentRepository.findByIdAndUserId(50L, 7L))
                .thenReturn(Optional.of(appointment));
        when(quoteRepository.findByAppointmentIdAndUserId(50L, 7L))
                .thenReturn(Optional.of(quote));
        when(workOrderRepository.findByAppointmentIdAndUserId(50L, 7L))
                .thenReturn(Optional.of(workOrder));

        useCase.execute(7L, 50L);

        assertThat(quote.getAppointment()).isNull();
        assertThat(workOrder.getAppointment()).isNull();
        InOrder order = inOrder(appointmentRepository);
        order.verify(appointmentRepository).flush();
        order.verify(appointmentRepository).delete(appointment);
    }

    @Test
    void rejectsAppointmentFromAnotherUser() {
        when(appointmentRepository.findByIdAndUserId(50L, 7L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(7L, 50L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No se encontró el turno indicado");

        verify(quoteRepository, never()).findByAppointmentIdAndUserId(50L, 7L);
        verify(workOrderRepository, never()).findByAppointmentIdAndUserId(50L, 7L);
        verify(appointmentRepository, never()).delete(any(Appointment.class));
    }
}
