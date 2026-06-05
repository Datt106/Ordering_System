package com.orderingsystem.uc007.support;

import com.orderingsystem.core.domain.PurchaseOrder;
import com.orderingsystem.infrastructure.database.PurchaseOrderRepository;
import com.orderingsystem.uc007.boundary.dto.OrderSplitLineDto;

import java.util.ArrayList;
import java.util.List;

public class PurchaseOrderPersister {

    private final PurchaseOrderRepository purchaseOrderRepository;

    public PurchaseOrderPersister(PurchaseOrderRepository purchaseOrderRepository) {
        this.purchaseOrderRepository = purchaseOrderRepository;
    }

    public void persist(String requestId, List<OrderSplitLineDto> lines) {
        purchaseOrderRepository.deleteByRequestId(requestId);
        List<PurchaseOrder> orders = new ArrayList<>();
        int seq = 1;
        for (OrderSplitLineDto line : lines) {
            orders.add(new PurchaseOrder(
                    buildOrderId(requestId, seq++),
                    requestId,
                    line.siteCode(),
                    line.merchandiseCode(),
                    line.quantity(),
                    line.unit(),
                    line.deliveryMeans()
            ));
        }
        purchaseOrderRepository.saveAll(orders);
    }

    private static String buildOrderId(String requestId, int sequence) {
        return "PO-" + requestId + "-" + sequence;
    }
}
