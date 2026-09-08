package com.mechanicmanager.quotes.dto;

import java.util.List;

public record QuotePageResponse(
        List<QuoteSummaryResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
