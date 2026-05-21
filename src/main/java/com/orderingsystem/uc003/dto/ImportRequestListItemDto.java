package com.orderingsystem.uc003.dto;

import com.orderingsystem.domain.request.RequestStatus;

import java.time.Instant;

/** Dòng danh sách theo dõi yêu cầu (UC003 — FR-02.2). */
public record ImportRequestListItemDto(
        String requestId,
        Instant createdAt,
        int itemCount,
        RequestStatus status
) {
}
