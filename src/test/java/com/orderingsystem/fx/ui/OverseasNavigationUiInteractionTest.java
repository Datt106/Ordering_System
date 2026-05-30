package com.orderingsystem.fx.ui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

class OverseasNavigationUiInteractionTest extends FxUiTestSupport {

    @BeforeEach
    void loginOverseas() {
        loginAs("overseas", "overseas123");
    }

    @Test
    void orderSplitScreenHasManualPlanControls() {
        openMenuExpectingTitle("Tách đơn hàng", "Tách đơn hàng");
        verifyThat("Sinh phương án tự động", isVisible());
        verifyThat("Kiểm tra phương án", isVisible());
        verifyThat("#planTable", isVisible());
        verifyThat("#processingTable", isVisible());
    }

    @Test
    void orderDispatchScreenHasSendControls() {
        openMenuExpectingTitle("Gửi đơn hàng", "Gửi đơn hàng");
        verifyThat("Xem trước", isVisible());
        verifyThat("Gửi đơn", isVisible());
        verifyThat("#ordersTable", isVisible());
        verifyThat("#requestTable", isVisible());
    }
}
