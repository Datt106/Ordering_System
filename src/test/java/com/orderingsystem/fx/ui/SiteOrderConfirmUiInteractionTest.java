package com.orderingsystem.fx.ui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

class SiteOrderConfirmUiInteractionTest extends FxUiTestSupport {

    @BeforeEach
    void loginSite() {
        loginAs("site01", "site123");
    }

    @Test
    void siteOrderConfirmScreenHasActionButtons() {
        openMenuExpectingTitle("Tiếp nhận đơn hàng", "Tiếp nhận đơn hàng");
        verifyThat("Làm mới", isVisible());
        verifyThat("Xác nhận", isVisible());
        verifyThat("Từ chối", isVisible());
        verifyThat("#table", isVisible());
    }
}
