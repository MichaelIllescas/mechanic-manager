package com.mechanicmanager.workorders.service;

import com.mechanicmanager.workorders.dto.WorkOrderPageResponse;
import com.mechanicmanager.workorders.exception.InvalidWorkOrderException;
import com.mechanicmanager.workorders.mapper.WorkOrderMapper;
import com.mechanicmanager.workorders.model.WorkOrder;
import com.mechanicmanager.workorders.model.WorkOrderStatus;
import com.mechanicmanager.workorders.repository.WorkOrderRepository;
import com.mechanicmanager.workorders.repository.WorkOrderSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class SearchWorkOrdersUseCase {

    private static final int MAX_PAGE_SIZE = 50;

    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderMapper workOrderMapper;

    @Transactional(readOnly = true)
    public WorkOrderPageResponse execute(
            Long userId,
            String search,
            WorkOrderStatus status,
            Long vehicleId,
            LocalDate from,
            LocalDate to,
            int page,
            int size
    ) {
        validatePagination(page, size);

        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "performedAt")
        );

        Page<WorkOrder> result = workOrderRepository.findAll(
                WorkOrderSpecifications.belongsToUserAndMatches(
                        userId,
                        search,
                        status,
                        vehicleId,
                        from,
                        to
                ),
                pageRequest
        );

        return new WorkOrderPageResponse(
                result.getContent().stream()
                        .map(workOrderMapper::toSummaryResponse)
                        .toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new InvalidWorkOrderException(
                    "El número de página no puede ser negativo"
            );
        }

        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new InvalidWorkOrderException(
                    "El tamaño de página debe estar entre 1 y " + MAX_PAGE_SIZE
            );
        }
    }
}
