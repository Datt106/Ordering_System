package com.orderingsystem.uc002.boundary.dto;

import com.orderingsystem.core.domain.ImportRequest;
import com.orderingsystem.core.domain.RequestStatus;

import java.time.Instant;
import java.util.List;

public record ImportRequestDto(
        String requestId,
        Instant createdAt,
        String createdBy,
        String department,
        RequestStatus status,
        String processedBy,
        Instant processedAt,
        List<ImportRequestItemDto> items
) {
    public static ImportRequestDto from(ImportRequest request) {
        return new ImportRequestDto(
                request.getRequestId(),
                request.getCreatedAt(),
                request.getCreatedBy(),
                request.getDepartment(),
                request.getStatus(),
                request.getProcessedBy(),
                request.getProcessedAt(),
                request.getItems().stream().map(ImportRequestItemDto::from).toList()
        );
    }

    /** Danh sách chờ xử lý — không load dòng mặt hàng. */
    public static ImportRequestDto fromSummary(ImportRequest request) {
        return new ImportRequestDto(
                request.getRequestId(),
                request.getCreatedAt(),
                request.getCreatedBy(),
                request.getDepartment(),
                request.getStatus(),
                request.getProcessedBy(),
                request.getProcessedAt(),
                List.of()
        );
    }
}
