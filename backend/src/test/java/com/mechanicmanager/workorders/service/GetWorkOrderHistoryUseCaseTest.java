package com.mechanicmanager.workorders.service;

import com.mechanicmanager.workorders.dto.WorkOrderResponse;
import com.mechanicmanager.workorders.exception.InvalidWorkOrderException;
import com.mechanicmanager.workorders.mapper.WorkOrderMapper;
import com.mechanicmanager.workorders.model.WorkOrder;
import com.mechanicmanager.workorders.repository.WorkOrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetWorkOrderHistoryUseCaseTest {

    @Mock
    private WorkOrderRepository workOrderRepository;

    @Mock
    private WorkOrderMapper workOrderMapper;

    @InjectMocks
    private GetWorkOrderHistoryUseCase useCase;

    @Test
    void returnsVehicleHistoryScopedToUser() {
        WorkOrder workOrder = new WorkOrder();
        WorkOrderResponse response = mock(WorkOrderResponse.class);

        when(workOrderRepository.findByUserIdAndVehicleIdOrderByPerformedAtDescIdDesc(3L, 7L))
                .thenReturn(List.of(workOrder));
        when(workOrderMapper.toResponse(workOrder)).thenReturn(response);

        assertThat(useCase.execute(3L, null, 7L)).containsExactly(response);
    }

    @Test
    void returnsCustomerHistoryScopedToUser() {
        WorkOrder workOrder = new WorkOrder();
        WorkOrderResponse response = mock(WorkOrderResponse.class);

        when(workOrderRepository.findByUserIdAndVehicleCustomerIdOrderByPerformedAtDescIdDesc(3L, 5L))
                .thenReturn(List.of(workOrder));
        when(workOrderMapper.toResponse(workOrder)).thenReturn(response);

        assertThat(useCase.execute(3L, 5L, null)).containsExactly(response);
        verify(workOrderRepository)
                .findByUserIdAndVehicleCustomerIdOrderByPerformedAtDescIdDesc(3L, 5L);
    }

    @Test
    void rejectsMissingOrBothFilters() {
        assertThatThrownBy(() -> useCase.execute(3L, null, null))
                .isInstanceOf(InvalidWorkOrderException.class);
        assertThatThrownBy(() -> useCase.execute(3L, 1L, 2L))
                .isInstanceOf(InvalidWorkOrderException.class);
        verifyNoInteractions(workOrderRepository);
    }
}
