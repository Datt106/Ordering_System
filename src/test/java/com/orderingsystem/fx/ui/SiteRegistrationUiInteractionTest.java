package com.orderingsystem.fx.ui;

import org.junit.jupiter.api.Test;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

class SiteRegistrationUiInteractionTest extends FxUiTestSupport {

    @Test
    void openSiteRegisterFromLogin() {
        clickOn("Đăng ký tài khoản Site…");
        verifyThat(".title", hasText("Đăng ký tài khoản Site"));
        verifyThat("#siteCombo", isVisible());
    }

    @Test
    void backToLoginFromRegister() {
        clickOn("Đăng ký tài khoản Site…");
        clickOn("Quay lại đăng nhập");
        verifyThat(".title", hasText("Hệ thống Đặt hàng Nhập khẩu"));
    }
}
