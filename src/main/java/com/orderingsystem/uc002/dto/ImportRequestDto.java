package com.orderingsystem.uc002.dto;

import com.orderingsystem.domain.request.ImportRequest;
import com.orderingsystem.domain.request.RequestStatus;

import java.time.Instant;
import java.util.List;

public record ImportRequestDto(
        String requestId,
        Instant createdAt,
        String createdBy,
        String department,
        RequestStatus status,
        List<ImportRequestItemDto> items
) {
    public static ImportRequestDto from(ImportRequest request) {
        return new ImportRequestDto(
                request.getRequestId(),
                request.getCreatedAt(),
                request.getCreatedBy(),
                request.getDepartment(),
                request.getStatus(),
                request.getItems().stream().map(ImportRequestItemDto::from).toList()
        );
    }
}
