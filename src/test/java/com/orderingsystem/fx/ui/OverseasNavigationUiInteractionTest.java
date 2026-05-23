package com.orderingsystem.fx.ui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

class OverseasNavigationUiInteractionTest extends FxUiTestSupport {

    @BeforeEach
    void loginOverseas() {
        loginAs("overseas", "overseas123");
    }

    @Test
    void navigatesToSitesScreen() {
        openMenuExpectingTitle("Quản lý Site", "Quản lý Site");
    }

    @Test
    void navigatesToPendingRequestsScreen() {
        openMenuExpectingTitle("Tiếp nhận yêu cầu", "Tiếp nhận yêu cầu");
        verifyThat("Làm mới", hasText("Làm mới"));
        verifyThat("Tiếp nhận", hasText("Tiếp nhận"));
    }

    @Test
    void navigatesToInventoryQueryScreen() {
        openMenuExpectingTitle("Truy vấn tồn kho", "Truy vấn tồn kho");
        verifyThat("Gửi truy vấn", hasText("Gửi truy vấn"));
        verifyThat("Xem trạng thái", hasText("Xem trạng thái"));
    }

}
