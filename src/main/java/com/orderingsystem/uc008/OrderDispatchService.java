package com.orderingsystem.uc008;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.infrastructure.database.ImportRequestRepository;
import com.orderingsystem.infrastructure.database.PurchaseOrderRepository;
import com.orderingsystem.uc008.controller.OrderDispatchController;

/** @deprecated Dùng OrderDispatchController (BCE). */
@Deprecated
public class OrderDispatchService extends OrderDispatchController {

    public OrderDispatchService() {
        super();
    }

    public OrderDispatchService(
            AuthService authService,
            PurchaseOrderRepository purchaseOrderRepository,
            ImportRequestRepository importRequestRepository
    ) {
        super(authService, purchaseOrderRepository, importRequestRepository);
    }
}
