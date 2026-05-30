package com.orderingsystem.fx.ui;

import com.orderingsystem.auth.Session;
import com.orderingsystem.fx.app.AppContext;
import com.orderingsystem.fx.navigation.Navigator;
import com.orderingsystem.infrastructure.database.DbManager;
import com.orderingsystem.infrastructure.database.SchemaInitializer;
import com.orderingsystem.infrastructure.seed.DatabaseSeeder;
import javafx.scene.control.ListView;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

/**
 * Khởi chạy ứng dụng thật (Navigator + DB seed) cho kiểm thử tương tác JavaFX.
 */
@Tag("ui")
abstract class FxUiTestSupport extends ApplicationTest {

    private static Navigator navigator;

    @Override
    public void start(Stage stage) throws Exception {
        DbManager.init("jdbc:sqlite:data/test-ordering.db", "", "");
        SchemaInitializer.apply();
        new DatabaseSeeder().seedDemoData();
        AppContext appContext = new AppContext();
        navigator = new Navigator(appContext);
        navigator.start(stage);
    }

    protected void loginAs(String username, String password) {
        try {
            WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> {
                try {
                    return lookup("#usernameField").query().isVisible();
                } catch (Exception e) {
                    return false;
                }
            });
        } catch (TimeoutException ex) {
            throw new AssertionError("Màn hình đăng nhập chưa sẵn sàng.", ex);
        }
        replaceText("#usernameField", username);
        replaceText("#passwordField", password);
        clickOn("Đăng nhập");
        WaitForAsyncUtils.waitForFxEvents();
        waitForRoleShell();
    }

    private void replaceText(String selector, String text) {
        clickOn(selector);
        push(KeyCode.CONTROL, KeyCode.A);
        write(text);
    }

    protected void waitForRoleShell() {
        try {
            WaitForAsyncUtils.waitFor(30, TimeUnit.SECONDS, () -> lookup("#roleLabel").query() != null);
            verifyThat("#roleLabel", isVisible());
        } catch (TimeoutException ex) {
            throw new AssertionError("Không mở được màn hình chính sau đăng nhập.", ex);
        }
    }

    protected void openMenuExpectingTitle(String menuLabel, String screenTitle) {
        interact(() -> {
            ListView<String> menu = (ListView<String>) lookup("#menuList").query();
            int index = menu.getItems().indexOf(menuLabel);
            if (index < 0) {
                throw new IllegalStateException("Menu không có mục: " + menuLabel);
            }
            menu.getSelectionModel().select(index);
        });
        try {
            WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> {
                try {
                    Label title = (Label) lookup(".screen-title").query();
                    return screenTitle.equals(title.getText());
                } catch (Exception e) {
                    return false;
                }
            });
        } catch (TimeoutException ex) {
            throw new AssertionError("Không tải được màn hình: " + screenTitle, ex);
        }
        verifyThat(".screen-title", hasText(screenTitle));
    }

    protected void closeModalWindows() {
        interact(() -> Window.getWindows().stream()
                .filter(Window::isShowing)
                .filter(w -> w instanceof Stage stage && stage.getOwner() != null)
                .forEach(Window::hide));
        WaitForAsyncUtils.waitForFxEvents();
    }

    @BeforeEach
    void resetToLoginScreen() throws Exception {
        closeModalWindows();
        Session.clear();
        interact(() -> {
            try {
                navigator.showLogin();
            } catch (java.io.IOException e) {
                throw new IllegalStateException(e);
            }
        });
        WaitForAsyncUtils.waitForFxEvents();
        try {
            WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> {
                try {
                    return lookup("#usernameField").query().isVisible();
                } catch (Exception e) {
                    return false;
                }
            });
        } catch (TimeoutException ex) {
            throw new AssertionError("Không quay lại được màn hình đăng nhập.", ex);
        }
    }

    @AfterEach
    void cleanupUiSession() {
        closeModalWindows();
        Session.clear();
    }
}
