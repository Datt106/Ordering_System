package com.orderingsystem.uc014;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.core.domain.OrderStatus;
import com.orderingsystem.infrastructure.database.DbManager;
import com.orderingsystem.infrastructure.database.PurchaseOrderRepository;
import com.orderingsystem.infrastructure.database.SchemaInitializer;
import com.orderingsystem.infrastructure.seed.DatabaseSeeder;
import com.orderingsystem.uc013.WarehouseOrderViewService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WarehouseReconcileServiceTest {

    private static final AuthService authService = new AuthService();
    private static final PurchaseOrderRepository purchaseOrderRepository = new PurchaseOrderRepository();
    private static final WarehouseReconcileService warehouseReconcileService = new WarehouseReconcileService();
    private static final WarehouseOrderViewService warehouseOrderViewService = new WarehouseOrderViewService();

    @BeforeAll
    static void init() {
        DbManager.init("jdbc:sqlite:data/test-ordering.db", "", "");
        SchemaInitializer.apply();
        new DatabaseSeeder().seedDemoData();
    }

    @AfterAll
    static void shutdown() {
        DbManager.shutdown();
    }

    @Test
    void warehouseCanReconcileConfirmedOrder() {
        var anyOrder = purchaseOrderRepository.findAll().stream()
                .filter(o -> o.getStatus() == OrderStatus.DA_XAC_NHAN)
                .findFirst()
                .orElseGet(() -> {
                    var fallback = purchaseOrderRepository.findAll().stream().findFirst().orElseThrow();
                    fallback.setStatus(OrderStatus.DA_XAC_NHAN);
                    purchaseOrderRepository.save(fallback);
                    return fallback;
                });

        authService.logout();
        authService.login("warehouse", "wh123");
        var result = warehouseReconcileService.recordInbound(anyOrder.getOrderId(), anyOrder.getQuantityOrdered());

        assertEquals(OrderStatus.DA_NHAP_KHO, result.status());
        assertEquals(0, result.quantityDiff());

        var rows = warehouseOrderViewService.listOrders(OrderStatus.DA_NHAP_KHO, null, null);
        assertFalse(rows.isEmpty());
    }

    @Test
    void warehouseReconcile_requiresWarehouseRole() {
        authService.logout();
        authService.login("sales", "sales123");
        assertThrows(SecurityException.class, () -> warehouseReconcileService.recordInbound("PO-X", 1));
    }
}
