package com.orderingsystem.fx.presentation.shell;

import com.orderingsystem.auth.AuthenticatedUser;
import com.orderingsystem.fx.framework.FxmlLoaderFactory;
import com.orderingsystem.fx.navigation.ScreenDefinition;
import com.orderingsystem.fx.presentation.BaseViewController;
import com.orderingsystem.fx.presentation.UiTasks;
import com.orderingsystem.fx.presentation.ux.UiFeedback;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AppShellController extends BaseViewController {

    @FXML
    private Label roleLabel;
    @FXML
    private Label userLabel;
    @FXML
    private Label contextHelpLabel;
    @FXML
    private Label globalStatusLabel;
    @FXML
    private ProgressIndicator busyIndicator;
    @FXML
    private ListView<String> menuList;
    @FXML
    private StackPane contentPane;
    @FXML
    private Button logoutButton;

    private List<ScreenDefinition> screens;
    private Runnable onLogout;

    public void configure(AuthenticatedUser user, List<ScreenDefinition> screenDefinitions, String roleTitle) {
        this.screens = screenDefinitions;
        roleLabel.setText(roleTitle);
        userLabel.setText(user.username() + (user.siteCode() != null ? " · Site " + user.siteCode() : ""));
        menuList.getItems().setAll(screenDefinitions.stream().map(ScreenDefinition::label).toList());
        UiFeedback.bind(globalStatusLabel, busyIndicator);
        menuList.getSelectionModel().selectedIndexProperty().addListener((obs, old, idx) -> {
            if (idx != null && idx.intValue() >= 0) {
                showScreen(idx.intValue());
            }
        });
    }

    public void setOnLogout(Runnable onLogout) {
        this.onLogout = onLogout;
    }

    public void showFirstScreen() {
        if (!screens.isEmpty()) {
            menuList.getSelectionModel().select(0);
        }
    }

    @Override
    protected void initializeView(URL location, ResourceBundle resources) {
        logoutButton.setOnAction(e -> onLogoutClicked());
    }

    @FXML
    private void onLogoutClicked() {
        if (!UiTasks.confirm("Đăng xuất", "Kết thúc phiên làm việc?", "Bạn có thể đăng nhập lại bất cứ lúc nào.")) {
            return;
        }
        if (onLogout == null) {
            UiTasks.showError(new IllegalStateException("Chưa cấu hình đăng xuất. Khởi động lại ứng dụng."));
            return;
        }
        UiFeedback.clear();
        onLogout.run();
    }

    private void showScreen(int index) {
        ScreenDefinition screen = screens.get(index);
        contextHelpLabel.setText(screen.helpText());
        UiTasks.runWithStatusOnFxThread(
                "Đang mở: " + screen.label() + "…",
                () -> {
                    try {
                        FxmlLoaderFactory loader = new FxmlLoaderFactory(app);
                        contentPane.getChildren().setAll(loader.loadView(screen.fxmlClasspath()));
                        Platform.runLater(() -> {
                            try {
                                loader.initLastController();
                            } catch (Exception initEx) {
                                UiTasks.showError(initEx);
                            }
                        });
                    } catch (IOException ex) {
                        throw new IllegalStateException(
                                "Không tải được màn hình. Thử chọn lại menu.",
                                ex
                        );
                    }
                },
                "Đang xem: " + screen.label()
        );
    }
}
