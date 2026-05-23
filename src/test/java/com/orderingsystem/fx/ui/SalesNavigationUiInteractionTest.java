package com.orderingsystem.fx.ui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

class SalesNavigationUiInteractionTest extends FxUiTestSupport {

    @BeforeEach
    void loginSales() {
        loginAs("sales", "sales123");
    }

    @Test
    void navigatesAllSalesMenus() {
        openMenuExpectingTitle("Danh mục hàng chuẩn", "Danh mục mặt hàng chuẩn");
        openMenuExpectingTitle("Tạo yêu cầu nhập", "Tạo yêu cầu nhập hàng");
        openMenuExpectingTitle("Theo dõi yêu cầu", "Theo dõi yêu cầu");
    }

    @Test
    void createRequestScreenHasCoreControls() {
        openMenuExpectingTitle("Tạo yêu cầu nhập", "Tạo yêu cầu nhập hàng");
        verifyThat("#addLineButton", hasText("Thêm dòng"));
        verifyThat("#addLineButton", isVisible());
        verifyThat("#submitButton", hasText("Gửi yêu cầu"));
        verifyThat("#submitButton", isVisible());
    }
}
