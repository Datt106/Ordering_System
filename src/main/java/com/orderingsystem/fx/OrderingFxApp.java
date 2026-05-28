package com.orderingsystem.fx;

import com.orderingsystem.fx.app.AppContext;
import com.orderingsystem.fx.navigation.Navigator;
import com.orderingsystem.infrastructure.database.DbManager;
import com.orderingsystem.infrastructure.database.SchemaInitializer;
import com.orderingsystem.infrastructure.seed.DatabaseSeeder;
import javafx.application.Application;
import javafx.stage.Stage;

public class OrderingFxApp extends Application {

    private Navigator navigator;

    @Override
    public void init() {
        DbManager.init();
        SchemaInitializer.apply();
        new DatabaseSeeder().seedDemoData();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        AppContext appContext = new AppContext();
        navigator = new Navigator(appContext);
        navigator.start(primaryStage);
    }

    @Override
    public void stop() {
        DbManager.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
