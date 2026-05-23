package com.orderingsystem.fx.presentation.auth;

import com.orderingsystem.auth.AuthenticatedUser;
import com.orderingsystem.fx.navigation.Navigator;
import com.orderingsystem.fx.navigation.RoleMenuFactory;
import com.orderingsystem.fx.presentation.BaseViewController;
import com.orderingsystem.fx.presentation.UiTasks;
import com.orderingsystem.infrastructure.jpa.JpaBootstrap;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import com.orderingsystem.fx.presentation.ux.FormValidation;

import java.util.Optional;

public class LoginController extends BaseViewController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Label hintLabel;

    private Navigator navigator;

    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    @Override
    protected void onInit() {
        hintLabel.setText(RoleMenuFactory.loginHelpText().trim());
        FormValidation.bindDisabledUntilFilled(loginButton, usernameField, passwordField);
        setScreenStatus("Nhập tài khoản và mật khẩu để bắt đầu.");
    }

    @FXML
    private void onLogin() {
        try {
            FormValidation.requireNonBlank(usernameField, "Vui lòng nhập tên đăng nhập.", () -> setScreenStatus("Thiếu tên đăng nhập."));
            FormValidation.requireNonBlank(passwordField, "Vui lòng nhập mật khẩu.", () -> setScreenStatus("Thiếu mật khẩu."));
        } catch (IllegalArgumentException ex) {
            UiTasks.showError(ex);
            return;
        }
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        UiTasks.runWithStatus(
                "Đang đăng nhập…",
                () -> app.auth().login(username, password),
                this::handleLoginResult,
                "Đăng nhập thành công."
        );
    }

    private void handleLoginResult(Optional<AuthenticatedUser> user) {
        if (user.isEmpty()) {
            setScreenStatus("Đăng nhập thất bại.");
            UiTasks.showError(new IllegalArgumentException("Sai tên đăng nhập hoặc mật khẩu. Kiểm tra gợi ý tài khoản demo bên dưới."));
            return;
        }
        try {
            navigator.showDashboard(user.get());
        } catch (Exception ex) {
            app.auth().logout();
            UiTasks.showError(new IllegalStateException("Không mở được màn hình chính. " + ex.getMessage(), ex));
        }
    }

    @FXML
    private void onShowSystemInfo() {
        UiTasks.showInfo(
                "Thông tin hệ thống",
                "Cơ sở dữ liệu cục bộ:\n" + JpaBootstrap.databasePath()
                        + "\n\nPhiên bản demo — dùng cho phát triển và kiểm thử."
        );
    }
}
