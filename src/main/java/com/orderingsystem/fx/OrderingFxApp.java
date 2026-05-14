package com.orderingsystem.fx;

import com.orderingsystem.fx.framework.FxmlLoaderFactory;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class OrderingFxApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        var factory = new FxmlLoaderFactory();
        var root = factory.loadRoot("/fxml/MainView.fxml");
        var scene = new Scene(root, 960, 640);
        scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());

        primaryStage.setTitle("Ordering — JavaFX");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(720);
        primaryStage.setMinHeight(480);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
