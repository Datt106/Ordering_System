package com.orderingsystem.fx.presentation;

import com.orderingsystem.fx.app.AppContext;

/**
 * Controller FXML nhận dependency qua composition root (Creator / DIP).
 */
public interface ViewController {

    void init(AppContext appContext);
}
