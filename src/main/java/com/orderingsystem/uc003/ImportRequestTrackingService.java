package com.orderingsystem.uc003;

import com.orderingsystem.uc003.controller.RequestTrackController;
import com.orderingsystem.auth.AuthService;
import com.orderingsystem.infrastructure.database.ImportRequestRepository;
import com.orderingsystem.infrastructure.database.PurchaseOrderRepository;

/** @deprecated Dùng RequestTrackController (BCE). */
@Deprecated
public class ImportRequestTrackingService extends RequestTrackController {

    public ImportRequestTrackingService() {
        super();
    }

    public ImportRequestTrackingService(AuthService authService,
            ImportRequestRepository importRequestRepository,
            PurchaseOrderRepository purchaseOrderRepository) {
        super(authService, importRequestRepository, purchaseOrderRepository);
    }

}
