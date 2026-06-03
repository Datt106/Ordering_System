package com.orderingsystem.fx.presentation.shell;

import com.orderingsystem.auth.AuthenticatedUser;
import com.orderingsystem.fx.framework.FxmlLoaderFactory;
import com.orderingsystem.fx.navigation.ScreenDefinition;
import com.orderingsystem.fx.presentation.BaseViewController;
import com.orderingsystem.fx.presentation.UiTasks;
import com.orderingsystem.fx.presentation.ux.ScrollSupport;
import com.orderingsystem.fx.presentation.ux.UiFeedback;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
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
    private ProgressIndicator busyIndicator;
    @FXML
    private Label statusLabel;
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
        configureMenuList();
        UiFeedback.bind(statusLabel, busyIndicator);
        menuList.getSelectionModel().selectedIndexProperty().addListener((obs, old, idx) -> {
            if (idx != null && idx.intValue() >= 0 && idx.intValue() < screens.size()) {
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

    private void configureMenuList() {
        menuList.setFixedCellSize(44);
        menuList.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setDisable(empty);
                setMouseTransparent(empty);
            }
        });
        menuList.prefHeightProperty().bind(Bindings.createDoubleBinding(
                () -> Math.max(44, menuList.getItems().size() * menuList.getFixedCellSize() + 4),
                menuList.getItems(),
                menuList.fixedCellSizeProperty()));
        menuList.setMaxHeight(Double.MAX_VALUE);
    }

    private void showScreen(int index) {
        ScreenDefinition screen = screens.get(index);
        UiTasks.runWithStatusOnFxThread(
                "Đang mở: " + screen.label() + "…",
                () -> {
                    try {
                        FxmlLoaderFactory loader = new FxmlLoaderFactory(app);
                        ScrollPane scroll = ScrollSupport.wrapScreen(loader.loadView(screen.fxmlClasspath()));
                        scroll.setMaxWidth(Double.MAX_VALUE);
                        scroll.setMaxHeight(Double.MAX_VALUE);
                        contentPane.getChildren().setAll(scroll);
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
