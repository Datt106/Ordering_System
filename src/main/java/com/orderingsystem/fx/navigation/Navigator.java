package com.orderingsystem.fx.navigation;

import com.orderingsystem.auth.AuthenticatedUser;
import com.orderingsystem.fx.app.AppContext;
import com.orderingsystem.fx.framework.FxmlLoaderFactory;
import com.orderingsystem.fx.presentation.ViewController;
import com.orderingsystem.fx.presentation.shell.AppShellController;
import com.orderingsystem.fx.presentation.auth.LoginController;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * Điều hướng cấp ứng dụng (Controller GRASP) — không chứa logic nghiệp vụ.
 */
public final class Navigator {

    private final AppContext appContext;
    private final FxmlLoaderFactory fxmlLoader;
    private Stage primaryStage;
    private Scene scene;

    public Navigator(AppContext appContext) {
        this.appContext = Objects.requireNonNull(appContext);
        this.fxmlLoader = new FxmlLoaderFactory(appContext);
    }

    public void start(Stage stage) throws IOException {
        this.primaryStage = stage;
        showLogin();
    }

    public void showLogin() throws IOException {
        Parent root = fxmlLoader.load("/fxml/auth/LoginView.fxml");
        LoginController controller = fxmlLoader.getLastController();
        controller.setNavigator(this);
        setScene(root, "Đăng nhập — Ordering System");
    }

    public void showDashboard(AuthenticatedUser user) throws IOException {
        Parent root = fxmlLoader.load("/fxml/shell/AppShell.fxml");
        AppShellController shell = fxmlLoader.getLastController();
        shell.configure(user, RoleMenuFactory.screensFor(user.role()), RoleMenuFactory.roleTitle(user.role()));
        shell.setOnLogout(() -> {
            appContext.auth().logout();
            try {
                showLogin();
            } catch (IOException e) {
                throw new IllegalStateException("Không quay lại màn đăng nhập.", e);
            }
        });
        setScene(root, "Ordering — " + RoleMenuFactory.roleTitle(user.role()));
        shell.showFirstScreen();
    }

    private void setScene(Parent root, String title) {
        if (scene == null) {
            scene = new Scene(root);
            scene.getStylesheets().add(
                    Objects.requireNonNull(getClass().getResource("/css/app.css")).toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(960);
            primaryStage.setMinHeight(640);
        } else {
            scene.setRoot(root);
        }
        primaryStage.setTitle(title);
        if (!primaryStage.isShowing()) {
            primaryStage.show();
        }
    }

    public AppContext appContext() {
        return appContext;
    }

    public FxmlLoaderFactory fxmlLoader() {
        return fxmlLoader;
    }

    public void loadInto(ViewController host, String fxmlClasspath) throws IOException {
        fxmlLoader.load(fxmlClasspath);
    }
}
