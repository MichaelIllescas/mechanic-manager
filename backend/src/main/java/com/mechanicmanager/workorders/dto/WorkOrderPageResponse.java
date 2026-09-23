package com.mechanicmanager.workorders.dto;

import java.util.List;

public record WorkOrderPageResponse(
        List<WorkOrderSummaryResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
