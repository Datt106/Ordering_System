package com.orderingsystem.fx.ui.main;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MainController {

    @FXML
    private Label subtitleLabel;

    @FXML
    private void initialize() {
        subtitleLabel.setText("Chỉnh sửa FXML / Controller trong gói ui.* — mở rộng màn hình tại đây.");
    }
}
