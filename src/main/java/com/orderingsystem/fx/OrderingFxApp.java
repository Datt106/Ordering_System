package com.orderingsystem.fx;

import com.orderingsystem.fx.framework.FxmlLoaderFactory;
import com.orderingsystem.infrastructure.jpa.JpaBootstrap;
import com.orderingsystem.infrastructure.seed.DatabaseSeeder;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class OrderingFxApp extends Application {

    @Override
    public void init() {
        JpaBootstrap.init();
        new DatabaseSeeder().seedDemoData();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        var factory = new FxmlLoaderFactory();
        var root = factory.loadRoot("/fxml/MainView.fxml");
        var scene = new Scene(root, 960, 640);
        scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setMinWidth(720);
        primaryStage.setMinHeight(480);
        primaryStage.setTitle("Ordering — JavaFX | DB: " + JpaBootstrap.databasePath());
        primaryStage.show();
    }

    @Override
    public void stop() {
        JpaBootstrap.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
