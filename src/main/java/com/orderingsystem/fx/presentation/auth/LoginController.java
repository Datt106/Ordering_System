package com.orderingsystem.fx.presentation.auth;

import com.orderingsystem.auth.AuthenticatedUser;
import com.orderingsystem.fx.navigation.Navigator;
import com.orderingsystem.fx.presentation.BaseViewController;
import com.orderingsystem.fx.presentation.UiTasks;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
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
    private TextField passwordVisibleField;
    @FXML
    private Button loginButton;
    @FXML
    private Button togglePasswordButton;

    private Navigator navigator;

    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    @Override
    protected void onInit() {
        bindPasswordFields();
        FormValidation.bindDisabledUntilFilled(loginButton, usernameField, passwordField);
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
        String password = currentPasswordText();
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
            UiTasks.showError(new IllegalArgumentException("Sai tên đăng nhập hoặc mật khẩu."));
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
    private void onGoToSiteRegister() {
        try {
            navigator.showSiteRegister();
        } catch (Exception ex) {
            UiTasks.showError(new IllegalStateException("Không mở màn đăng ký Site.", ex));
        }
    }

    @FXML
    private void onTogglePasswordVisibility() {
        boolean showPlain = !passwordVisibleField.isVisible();
        if (showPlain) {
            passwordVisibleField.setText(passwordField.getText());
        } else {
            passwordField.setText(passwordVisibleField.getText());
        }
        passwordVisibleField.setVisible(showPlain);
        passwordVisibleField.setManaged(showPlain);
        passwordField.setVisible(!showPlain);
        passwordField.setManaged(!showPlain);
        togglePasswordButton.setText(showPlain ? "◉" : "○");
    }

    private void bindPasswordFields() {
        passwordVisibleField.textProperty().bindBidirectional(passwordField.textProperty());
        passwordVisibleField.setVisible(false);
        passwordVisibleField.setManaged(false);
        togglePasswordButton.setText("○");
    }

    private String currentPasswordText() {
        return passwordVisibleField.isVisible() ? passwordVisibleField.getText() : passwordField.getText();
    }
}
