package com.orderingsystem.uc003.boundary;

import com.orderingsystem.core.domain.RequestStatus;
import com.orderingsystem.uc003.boundary.dto.ImportRequestListItemDto;
import com.orderingsystem.uc003.boundary.dto.ImportRequestTrackingDetailDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Input boundary for the UC003 request-tracking use case.
 */
public interface RequestTrackingUseCase {

    List<ImportRequestListItemDto> listRequests();

    List<ImportRequestListItemDto> listRequests(
            RequestStatus status,
            LocalDate createdFrom,
            LocalDate createdTo
    );

    Optional<ImportRequestTrackingDetailDto> getRequestDetail(String requestId);
}
