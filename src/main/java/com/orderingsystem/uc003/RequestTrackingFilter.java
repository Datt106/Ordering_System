package com.orderingsystem.uc003;

import com.orderingsystem.core.domain.RequestStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

/**
 * Immutable filter value object for a UC003 request-tracking query.
 */
public record RequestTrackingFilter(
        RequestStatus status,
        LocalDate createdFrom,
        LocalDate createdTo
) {

    public RequestTrackingFilter {
        if (createdFrom != null && createdTo != null && createdFrom.isAfter(createdTo)) {
            throw new IllegalArgumentException("Ngày bắt đầu lọc không được sau ngày kết thúc.");
        }
    }

    public Instant createdFromInclusive(ZoneId zone) {
        Objects.requireNonNull(zone, "zone");
        return createdFrom == null ? null : createdFrom.atStartOfDay(zone).toInstant();
    }

    public Instant createdToExclusive(ZoneId zone) {
        Objects.requireNonNull(zone, "zone");
        return createdTo == null ? null : createdTo.plusDays(1).atStartOfDay(zone).toInstant();
    }
}
