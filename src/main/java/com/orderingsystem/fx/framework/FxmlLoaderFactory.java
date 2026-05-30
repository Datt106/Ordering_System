package com.orderingsystem.fx.framework;

import com.orderingsystem.fx.app.AppContext;
import com.orderingsystem.fx.presentation.ViewController;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

/**
 * Nạp FXML theo chuẩn {@link FXMLLoader}.
 * <p>
 * {@link #load(String)} — nạp + {@link ViewController#init} ngay (màn shell, login).
 * {@link #loadView(String)} — chỉ nạp FXML; gọi {@link #initLastController()} sau khi đã {@code setAll} vào scene.
 */
public final class FxmlLoaderFactory {

    private final AppContext appContext;
    private Object lastController;

    public FxmlLoaderFactory(AppContext appContext) {
        this.appContext = Objects.requireNonNull(appContext);
    }

    /** Nạp FXML và inject {@link AppContext} ngay (dùng cho login, shell). */
    public Parent load(String classpathFxml) throws IOException {
        Parent root = loadFxml(classpathFxml);
        initLastController();
        return root;
    }

    /**
     * Chỉ nạp FXML — {@link ViewController#init} phải gọi sau khi node đã trên scene graph
     * (tránh lỗi binding bảng / ComboBox khi mở menu con trong shell).
     */
    public Parent loadView(String classpathFxml) throws IOException {
        return loadFxml(classpathFxml);
    }

    public void initLastController() {
        if (lastController instanceof ViewController viewController) {
            viewController.init(appContext);
        }
    }

    private Parent loadFxml(String classpathFxml) throws IOException {
        URL url = Objects.requireNonNull(
                getClass().getResource(classpathFxml),
                "Không tìm thấy FXML: " + classpathFxml
        );
        FXMLLoader loader = new FXMLLoader(url);
        Parent root = loader.load();
        lastController = loader.getController();
        return root;
    }

    @SuppressWarnings("unchecked")
    public <T> T getLastController() {
        return (T) lastController;
    }
}
