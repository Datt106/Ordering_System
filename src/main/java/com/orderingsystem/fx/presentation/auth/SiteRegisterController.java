package com.orderingsystem.fx.presentation.auth;

import com.orderingsystem.auth.boundary.dto.RegistrableSiteDto;
import com.orderingsystem.core.domain.User;
import com.orderingsystem.fx.navigation.Navigator;
import com.orderingsystem.fx.presentation.BaseViewController;
import com.orderingsystem.fx.presentation.UiTasks;
import com.orderingsystem.fx.presentation.ux.FormValidation;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.List;

public class SiteRegisterController extends BaseViewController {

    @FXML
    private ComboBox<RegistrableSiteDto> siteCombo;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField passwordVisibleField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private TextField confirmPasswordVisibleField;
    @FXML
    private Button registerButton;
    @FXML
    private Button togglePasswordButton;
    @FXML
    private Button toggleConfirmPasswordButton;

    private Navigator navigator;

    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    @Override
    protected void onInit() {
        bindPasswordFields();
        siteCombo.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(RegistrableSiteDto item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.siteCode() + " — " + item.siteName());
            }
        });
        siteCombo.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(RegistrableSiteDto item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.siteCode() + " — " + item.siteName());
            }
        });
        FormValidation.bindDisabledUntilFilled(registerButton, usernameField, passwordField, confirmPasswordField);
        loadRegistrableSites();
    }

    @FXML
    private void onRegister() {
        RegistrableSiteDto site = siteCombo.getSelectionModel().getSelectedItem();
        if (site == null) {
            setScreenStatus("Chọn mã Site trong danh sách.");
            UiTasks.showError(new IllegalArgumentException(
                    "Chưa có Site khả dụng? Liên hệ Đặt hàng quốc tế để thêm mã Site trước khi đăng ký."));
            return;
        }
        try {
            FormValidation.requireNonBlank(usernameField, "Nhập tên đăng nhập.", () -> setScreenStatus("Thiếu tên đăng nhập."));
            FormValidation.requireNonBlank(passwordField, "Nhập mật khẩu.", () -> setScreenStatus("Thiếu mật khẩu."));
            FormValidation.requireNonBlank(confirmPasswordField, "Nhập lại mật khẩu.", () -> setScreenStatus("Thiếu xác nhận mật khẩu."));
        } catch (IllegalArgumentException ex) {
            UiTasks.showError(ex);
            return;
        }
        String password = currentPasswordText();
        String confirm = currentConfirmPasswordText();
        if (!password.equals(confirm)) {
            UiTasks.showError(new IllegalArgumentException("Mật khẩu nhập lại không khớp."));
            return;
        }

        String username = usernameField.getText().trim();
        String siteCode = site.siteCode();
        UiTasks.<User>runWithStatus(
                "Đang đăng ký…",
                () -> app.siteRegistration().registerSiteAccount(siteCode, username, password),
                user -> {
                    UiTasks.showInfo(
                            "Đăng ký thành công",
                            "Tài khoản " + user.getUsername() + " cho Site " + siteCode
                                    + ".\nĐăng nhập và khai báo vận chuyển + mặt hàng kinh doanh trước khi nhận truy vấn tồn kho.");
                    try {
                        navigator.showLogin();
                    } catch (Exception ex) {
                        UiTasks.showError(new IllegalStateException("Không quay lại màn đăng nhập.", ex));
                    }
                },
                "Hoàn tất."
        );
    }

    @FXML
    private void onBackToLogin() {
        try {
            navigator.showLogin();
        } catch (Exception ex) {
            UiTasks.showError(new IllegalStateException("Không mở màn đăng nhập.", ex));
        }
    }

    private void loadRegistrableSites() {
        UiTasks.<List<RegistrableSiteDto>>runWithStatus(
                "Đang tải danh sách Site…",
                () -> app.siteRegistration().listRegistrableSites(),
                sites -> {
                    siteCombo.setItems(FXCollections.observableArrayList(sites));
                    if (sites.isEmpty()) {
                        setScreenStatus("Không còn Site nào chờ đăng ký — tất cả đã có tài khoản hoặc chưa được Overseas thêm mã.");
                        registerButton.setDisable(true);
                    } else {
                        siteCombo.getSelectionModel().selectFirst();
                        setScreenStatus(sites.size() + " Site có thể đăng ký tài khoản.");
                    }
                },
                "Đã tải."
        );
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

    @FXML
    private void onToggleConfirmPasswordVisibility() {
        boolean showPlain = !confirmPasswordVisibleField.isVisible();
        if (showPlain) {
            confirmPasswordVisibleField.setText(confirmPasswordField.getText());
        } else {
            confirmPasswordField.setText(confirmPasswordVisibleField.getText());
        }
        confirmPasswordVisibleField.setVisible(showPlain);
        confirmPasswordVisibleField.setManaged(showPlain);
        confirmPasswordField.setVisible(!showPlain);
        confirmPasswordField.setManaged(!showPlain);
        toggleConfirmPasswordButton.setText(showPlain ? "◉" : "○");
    }

    private void bindPasswordFields() {
        passwordVisibleField.textProperty().bindBidirectional(passwordField.textProperty());
        confirmPasswordVisibleField.textProperty().bindBidirectional(confirmPasswordField.textProperty());
        passwordVisibleField.setVisible(false);
        passwordVisibleField.setManaged(false);
        confirmPasswordVisibleField.setVisible(false);
        confirmPasswordVisibleField.setManaged(false);
        togglePasswordButton.setText("○");
        toggleConfirmPasswordButton.setText("○");
    }

    private String currentPasswordText() {
        return passwordVisibleField.isVisible() ? passwordVisibleField.getText() : passwordField.getText();
    }

    private String currentConfirmPasswordText() {
        return confirmPasswordVisibleField.isVisible()
                ? confirmPasswordVisibleField.getText()
                : confirmPasswordField.getText();
    }
}
