package com.orderingsystem.fx.framework;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

/**
 * Nạp FXML với location chuẩn hóa từ classpath ({@code /fxml/...}).
 */
public final class FxmlLoaderFactory {

    public Parent loadRoot(String classpathFxml) throws IOException {
        URL url = Objects.requireNonNull(
                getClass().getResource(classpathFxml),
                "Không tìm thấy FXML: " + classpathFxml
        );
        return FXMLLoader.load(url);
    }

    public <T> T loadAndGetController(String classpathFxml) throws IOException {
        URL url = Objects.requireNonNull(
                getClass().getResource(classpathFxml),
                "Không tìm thấy FXML: " + classpathFxml
        );
        var loader = new FXMLLoader(url);
        loader.load();
        return loader.getController();
    }
}
