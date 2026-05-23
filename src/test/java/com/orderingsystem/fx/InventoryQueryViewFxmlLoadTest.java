package com.orderingsystem.fx;

import com.orderingsystem.auth.AuthenticatedUser;
import com.orderingsystem.auth.Session;
import com.orderingsystem.domain.auth.UserRole;
import com.orderingsystem.fx.app.AppContext;
import com.orderingsystem.fx.framework.FxmlLoaderFactory;
import com.orderingsystem.infrastructure.jpa.JpaBootstrap;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class InventoryQueryViewFxmlLoadTest {

    @BeforeAll
    static void initFx() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.startup(latch::countDown);
        latch.await(10, TimeUnit.SECONDS);
        JpaBootstrap.init();
    }

    @Test
    void loadsInventoryQueryView() {
        Session.setCurrentUser(new AuthenticatedUser(1L, "overseas", UserRole.OVERSEAS, null));
        FxmlLoaderFactory loader = new FxmlLoaderFactory(new AppContext());
        assertDoesNotThrow(() -> loader.load("/fxml/overseas/InventoryQueryView.fxml"));
    }
}
