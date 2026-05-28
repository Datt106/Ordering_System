package com.orderingsystem.uc003.boundary.dto;

import com.orderingsystem.core.domain.RequestStatus;

import java.time.Instant;

/** Dòng danh sách theo dõi yêu cầu (UC003 — FR-02.2). */
public record ImportRequestListItemDto(
        String requestId,
        Instant createdAt,
        int itemCount,
        RequestStatus status
) {
}
