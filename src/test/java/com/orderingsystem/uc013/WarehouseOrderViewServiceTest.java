package com.orderingsystem.uc013;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.core.domain.OrderStatus;
import com.orderingsystem.infrastructure.database.DbManager;
import com.orderingsystem.infrastructure.database.PurchaseOrderRepository;
import com.orderingsystem.infrastructure.database.SchemaInitializer;
import com.orderingsystem.infrastructure.seed.DatabaseSeeder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WarehouseOrderViewServiceTest {

    private static final AuthService authService = new AuthService();
    private static final WarehouseOrderViewService warehouseOrderViewService = new WarehouseOrderViewService();
    private static final PurchaseOrderRepository purchaseOrderRepository = new PurchaseOrderRepository();

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
    void listOrders_filtersByStatusSiteAndMerchandise() {
        var anyOrder = purchaseOrderRepository.findAll().stream().findFirst().orElseThrow();
        anyOrder.setStatus(OrderStatus.DA_XAC_NHAN);
        purchaseOrderRepository.save(anyOrder);
        String siteCode = anyOrder.getSiteCode();
        String merchandiseCode = anyOrder.getMerchandiseCode();

        authService.logout();
        authService.login("warehouse", "wh123");

        assertTrue(warehouseOrderViewService.listOrders(OrderStatus.DA_XAC_NHAN, siteCode, merchandiseCode)
                .stream()
                .anyMatch(o -> o.orderId().equals(anyOrder.getOrderId())));

        assertTrue(warehouseOrderViewService.listOrders(OrderStatus.DA_XAC_NHAN, "  " + siteCode + "  ", null)
                .stream()
                .anyMatch(o -> o.orderId().equals(anyOrder.getOrderId())));

        authService.logout();
    }

    @Test
    void listOrders_requiresWarehouseRole() {
        authService.logout();
        authService.login("sales", "sales123");
        assertThrows(SecurityException.class, () -> warehouseOrderViewService.listOrders(null, null, null));
    }
}
