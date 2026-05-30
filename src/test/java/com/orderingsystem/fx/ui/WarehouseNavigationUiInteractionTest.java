package com.orderingsystem.fx.ui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

class WarehouseNavigationUiInteractionTest extends FxUiTestSupport {

    @BeforeEach
    void loginWarehouse() {
        loginAs("warehouse", "wh123");
    }

    @Test
    void warehouseLoginOpensOrderListByDefault() {
        verifyThat("#roleLabel", hasText("Quản lý kho"));
        verifyThat("#userLabel", hasText("warehouse"));
        verifyThat(".screen-title", hasText("Danh sách đơn hàng"));
    }

    @Test
    void orderListScreenHasSearchControls() {
        openMenuExpectingTitle("Danh sách đơn hàng", "Danh sách đơn hàng");
        verifyThat("Tìm", hasText("Tìm"));
        verifyThat("Tìm", isVisible());
        verifyThat("#statusFilter", isVisible());
        verifyThat("#siteFilter", isVisible());
        verifyThat("#merchandiseFilter", isVisible());
        verifyThat("#table", isVisible());
    }

    @Test
    void logoutReturnsToLoginScreen() {
        clickOn("Đăng xuất");
        clickOn("OK");
        verifyThat("#usernameField", isVisible());
        verifyThat("Đăng nhập", isVisible());
    }

    @Test
    void reconcileScreenHasInboundControls() {
        openMenuExpectingTitle("Đối chiếu nhập kho", "Đối chiếu nhập kho");
        verifyThat("Làm mới", hasText("Làm mới"));
        verifyThat("Ghi nhận", hasText("Ghi nhận"));
        verifyThat("#actualSpinner", isVisible());
        verifyThat("#table", isVisible());
    }
}
