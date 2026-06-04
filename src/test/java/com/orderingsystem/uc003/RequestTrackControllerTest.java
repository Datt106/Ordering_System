package com.orderingsystem.uc003;

import com.orderingsystem.core.domain.RequestStatus;
import com.orderingsystem.uc003.boundary.RequestTrackingUseCase;
import com.orderingsystem.uc003.boundary.dto.ImportRequestListItemDto;
import com.orderingsystem.uc003.boundary.dto.ImportRequestTrackingDetailDto;
import com.orderingsystem.uc003.controller.RequestTrackController;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class RequestTrackControllerTest {

    @Test
    void delegatesRequestsToUseCase() {
        ImportRequestListItemDto allItem = new ImportRequestListItemDto(
                "REQ-ALL",
                Instant.EPOCH,
                1,
                RequestStatus.CHO_XU_LY
        );
        ImportRequestListItemDto filteredItem = new ImportRequestListItemDto(
                "REQ-FILTERED",
                Instant.EPOCH,
                2,
                RequestStatus.DANG_XU_LY
        );
        StubRequestTrackingUseCase useCase = new StubRequestTrackingUseCase(
                List.of(allItem),
                List.of(filteredItem)
        );
        RequestTrackController controller = new RequestTrackController(useCase);

        assertEquals(List.of(allItem), controller.listRequests());

        LocalDate from = LocalDate.of(2026, 6, 1);
        LocalDate to = LocalDate.of(2026, 6, 4);
        assertEquals(
                List.of(filteredItem),
                controller.listRequests(RequestStatus.DANG_XU_LY, from, to)
        );
        assertSame(RequestStatus.DANG_XU_LY, useCase.status);
        assertEquals(from, useCase.createdFrom);
        assertEquals(to, useCase.createdTo);

        assertEquals(Optional.empty(), controller.getRequestDetail("REQ-DETAIL"));
        assertEquals("REQ-DETAIL", useCase.requestId);
    }

    private static final class StubRequestTrackingUseCase implements RequestTrackingUseCase {

        private final List<ImportRequestListItemDto> allRequests;
        private final List<ImportRequestListItemDto> filteredRequests;
        private RequestStatus status;
        private LocalDate createdFrom;
        private LocalDate createdTo;
        private String requestId;

        private StubRequestTrackingUseCase(
                List<ImportRequestListItemDto> allRequests,
                List<ImportRequestListItemDto> filteredRequests
        ) {
            this.allRequests = allRequests;
            this.filteredRequests = filteredRequests;
        }

        @Override
        public List<ImportRequestListItemDto> listRequests() {
            return allRequests;
        }

        @Override
        public List<ImportRequestListItemDto> listRequests(
                RequestStatus status,
                LocalDate createdFrom,
                LocalDate createdTo
        ) {
            this.status = status;
            this.createdFrom = createdFrom;
            this.createdTo = createdTo;
            return filteredRequests;
        }

        @Override
        public Optional<ImportRequestTrackingDetailDto> getRequestDetail(String requestId) {
            this.requestId = requestId;
            return Optional.empty();
        }
    }
}
