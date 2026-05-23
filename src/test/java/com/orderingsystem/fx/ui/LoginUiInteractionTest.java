package com.orderingsystem.fx.ui;

import org.junit.jupiter.api.Test;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

class LoginUiInteractionTest extends FxUiTestSupport {

    @Test
    void showsLoginScreenOnStartup() {
        verifyThat(".title", hasText("Hệ thống Đặt hàng Nhập khẩu"));
        verifyThat("#usernameField", org.testfx.matcher.control.TextInputControlMatchers.hasText(""));
        verifyThat("#loginButton", javafx.scene.Node::isDisable);
    }

    @Test
    void enablesLoginWhenCredentialsFilled() {
        clickOn("#usernameField").write("sales");
        clickOn("#passwordField").write("sales123");
        verifyThat("#loginButton", node -> !node.isDisable());
    }

    @Test
    void overseasLoginOpensDashboard() {
        loginAs("overseas", "overseas123");
        verifyThat("#roleLabel", hasText("Đặt hàng quốc tế"));
        verifyThat("#userLabel", hasText("overseas"));
    }

    @Test
    void salesLoginOpensCatalogByDefault() {
        loginAs("sales", "sales123");
        verifyThat("#roleLabel", hasText("Bộ phận Bán hàng"));
        verifyThat(".screen-title", hasText("Danh mục mặt hàng chuẩn"));
    }

    @Test
    void siteLoginOpensShippingScreen() {
        loginAs("site01", "site123");
        verifyThat("#roleLabel", hasText("Site nhập khẩu"));
        verifyThat(".screen-title", hasText("Thông tin vận chuyển"));
    }
}
