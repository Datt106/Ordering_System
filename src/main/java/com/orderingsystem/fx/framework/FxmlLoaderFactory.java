package com.orderingsystem.fx.framework;

import com.orderingsystem.fx.app.AppContext;
import com.orderingsystem.fx.presentation.ViewController;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

/**
 * Nạp FXML theo chuẩn {@link FXMLLoader}: inject {@code @FXML}, gọi {@link javafx.fxml.Initializable},
 * rồi inject {@link AppContext} qua {@link ViewController#init}.
 * <p>Phải gọi {@link #load} trên JavaFX Application Thread vì tạo {@link Parent}.
 */
public final class FxmlLoaderFactory {

    private final AppContext appContext;
    private Object lastController;

    public FxmlLoaderFactory(AppContext appContext) {
        this.appContext = Objects.requireNonNull(appContext);
    }

    public Parent load(String classpathFxml) throws IOException {
        URL url = Objects.requireNonNull(
                getClass().getResource(classpathFxml),
                "Không tìm thấy FXML: " + classpathFxml
        );
        FXMLLoader loader = new FXMLLoader(url);
        Parent root = loader.load();
        lastController = loader.getController();
        if (lastController instanceof ViewController viewController) {
            viewController.init(appContext);
        }
        return root;
    }

    @SuppressWarnings("unchecked")
    public <T> T getLastController() {
        return (T) lastController;
    }
}
