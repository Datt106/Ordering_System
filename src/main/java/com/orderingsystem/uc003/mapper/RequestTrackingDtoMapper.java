package com.orderingsystem.uc003.mapper;

import com.orderingsystem.core.domain.ImportRequest;
import com.orderingsystem.core.domain.ImportRequestItem;
import com.orderingsystem.core.domain.PurchaseOrder;
import com.orderingsystem.uc002.boundary.dto.ImportRequestDto;
import com.orderingsystem.uc003.boundary.dto.ImportRequestListItemDto;
import com.orderingsystem.uc003.boundary.dto.ImportRequestTrackingDetailDto;
import com.orderingsystem.uc003.boundary.dto.PurchaseOrderTrackingDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Maps domain objects to the read-only DTOs exposed by UC003.
 */
public final class RequestTrackingDtoMapper {

    public ImportRequestListItemDto toListItem(ImportRequest request, long itemCount) {
        Objects.requireNonNull(request, "request");
        return new ImportRequestListItemDto(
                request.getRequestId(),
                request.getCreatedAt(),
                (int) itemCount,
                request.getStatus()
        );
    }

    public ImportRequestTrackingDetailDto toDetail(
            ImportRequest request,
            List<PurchaseOrder> childOrders
    ) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(childOrders, "childOrders");

        List<PurchaseOrderTrackingDto> childOrderDtos = childOrders.isEmpty()
                ? List.of()
                : toChildOrderDtos(request, childOrders);

        return new ImportRequestTrackingDetailDto(
                ImportRequestDto.from(request),
                childOrderDtos
        );
    }

    private List<PurchaseOrderTrackingDto> toChildOrderDtos(
            ImportRequest request,
            List<PurchaseOrder> childOrders
    ) {
        Map<String, LocalDate> deliveryByMerchandise = request.getItems().stream()
                .collect(Collectors.toMap(
                        ImportRequestItem::getMerchandiseCode,
                        ImportRequestItem::getDesiredDeliveryDate,
                        (first, second) -> first.isBefore(second) ? first : second));

        return childOrders.stream()
                .map(order -> toChildOrderDto(
                        order,
                        deliveryByMerchandise.get(order.getMerchandiseCode())))
                .toList();
    }

    private PurchaseOrderTrackingDto toChildOrderDto(
            PurchaseOrder order,
            LocalDate expectedDeliveryDate
    ) {
        return new PurchaseOrderTrackingDto(
                order.getOrderId(),
                order.getSiteCode(),
                order.getMerchandiseCode(),
                order.getQuantityOrdered(),
                order.getUnit(),
                order.getDeliveryMeans(),
                order.getDeliveryMeans().toExternalValue(),
                order.getStatus(),
                expectedDeliveryDate,
                order.getActualQuantity(),
                order.getQuantityDiff()
        );
    }
}
