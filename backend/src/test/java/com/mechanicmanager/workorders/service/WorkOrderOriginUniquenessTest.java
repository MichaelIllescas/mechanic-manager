package com.mechanicmanager.workorders.service;

import com.mechanicmanager.appointments.model.Appointment;
import com.mechanicmanager.appointments.repository.AppointmentRepository;
import com.mechanicmanager.quotes.model.Quote;
import com.mechanicmanager.quotes.model.QuoteStatus;
import com.mechanicmanager.quotes.repository.QuoteRepository;
import com.mechanicmanager.users.model.User;
import com.mechanicmanager.users.repository.UserRepository;
import com.mechanicmanager.vehicles.model.Vehicle;
import com.mechanicmanager.vehicles.repository.VehicleRepository;
import com.mechanicmanager.workorders.dto.CreateWorkOrderRequest;
import com.mechanicmanager.workorders.dto.WorkOrderItemRequest;
import com.mechanicmanager.workorders.exception.WorkOrderAlreadyExistsException;
import com.mechanicmanager.workorders.mapper.WorkOrderMapper;
import com.mechanicmanager.workorders.model.WorkOrder;
import com.mechanicmanager.workorders.repository.WorkOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkOrderOriginUniquenessTest {

    private static final Long USER_ID = 7L;

    @Mock
    private WorkOrderRepository workOrderRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private QuoteRepository quoteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WorkOrderItemFactory workOrderItemFactory;

    @Mock
    private WorkOrderMapper workOrderMapper;

    private final Vehicle vehicle = Vehicle.builder().id(3L).build();

    private CreateWorkOrderUseCase createUseCase;
    private CreateWorkOrderFromAppointmentUseCase fromAppointmentUseCase;

    @BeforeEach
    void setUp() {
        createUseCase = new CreateWorkOrderUseCase(
                workOrderRepository, vehicleRepository, appointmentRepository, quoteRepository,
                userRepository, workOrderItemFactory, workOrderMapper);
        fromAppointmentUseCase =
                new CreateWorkOrderFromAppointmentUseCase(workOrderRepository);
    }

    private CreateWorkOrderRequest request(Long appointmentId, Long quoteId) {
        return new CreateWorkOrderRequest(
                3L, appointmentId, quoteId, LocalDate.of(2026, 9, 24), null, null,
                List.of(new WorkOrderItemRequest(
                        null, null, "Cambio de aceite", BigDecimal.ONE, BigDecimal.TEN)));
    }

    private void givenUserAndVehicle() {
        when(userRepository.findOneById(USER_ID)).thenReturn(Optional.of(User.builder().id(USER_ID).build()));
        when(vehicleRepository.findByIdAndUserId(3L, USER_ID)).thenReturn(Optional.of(vehicle));
    }

    @Test
    void rejectsCreatingASecondWorkOrderForTheSameAppointment() {
        givenUserAndVehicle();
        when(appointmentRepository.findByIdAndUserId(11L, USER_ID))
                .thenReturn(Optional.of(Appointment.builder().id(11L).vehicle(vehicle).build()));
        when(workOrderRepository.existsByAppointmentIdAndUserId(11L, USER_ID)).thenReturn(true);

        assertThatThrownBy(() -> createUseCase.execute(USER_ID, request(11L, null)))
                .isInstanceOf(WorkOrderAlreadyExistsException.class);

        verify(workOrderRepository, never()).save(any(WorkOrder.class));
    }

    @Test
    void rejectsCreatingASecondWorkOrderForTheSameQuote() {
        givenUserAndVehicle();
        when(quoteRepository.findByIdAndUserId(21L, USER_ID))
                .thenReturn(Optional.of(Quote.builder().id(21L).vehicle(vehicle).build()));
        when(workOrderRepository.existsByQuoteIdAndUserId(21L, USER_ID)).thenReturn(true);

        assertThatThrownBy(() -> createUseCase.execute(USER_ID, request(null, 21L)))
                .isInstanceOf(WorkOrderAlreadyExistsException.class);

        verify(workOrderRepository, never()).save(any(WorkOrder.class));
    }

    @Test
    void completingAnAppointmentDoesNotCreateAnotherWorkOrderIfOneExists() {
        Appointment appointment = Appointment.builder().id(11L).vehicle(vehicle).build();
        when(workOrderRepository.existsByAppointmentIdAndUserId(11L, USER_ID)).thenReturn(true);

        fromAppointmentUseCase.execute(USER_ID, appointment);

        verify(workOrderRepository, never()).save(any(WorkOrder.class));
    }

    @Test
    void completingAnAppointmentIgnoresAQuoteAlreadyUsedByAnotherWorkOrder() {
        Quote quote = Quote.builder().id(21L).status(QuoteStatus.APPROVED).build();
        Appointment appointment = Appointment.builder()
                .id(11L).vehicle(vehicle).user(User.builder().id(USER_ID).build()).quote(quote).build();
        when(workOrderRepository.existsByAppointmentIdAndUserId(11L, USER_ID)).thenReturn(false);
        when(workOrderRepository.existsByQuoteIdAndUserId(21L, USER_ID)).thenReturn(true);

        fromAppointmentUseCase.execute(USER_ID, appointment);

        ArgumentCaptor<WorkOrder> saved = ArgumentCaptor.forClass(WorkOrder.class);
        verify(workOrderRepository).save(saved.capture());
        assertThat(saved.getValue().getQuote()).isNull();
        assertThat(saved.getValue().getItems()).isEmpty();
    }
}
