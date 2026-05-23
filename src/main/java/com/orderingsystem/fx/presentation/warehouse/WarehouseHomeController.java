package com.orderingsystem.fx.presentation.warehouse;

import com.orderingsystem.fx.presentation.BaseViewController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class WarehouseHomeController extends BaseViewController {

    @FXML
    private Label messageLabel;

    @Override
    protected void onInit() {
        messageLabel.setText(
                "Chức năng kho (xem danh sách đơn UC013, đối chiếu nhập UC014) sẽ có trên giao diện "
                        + "khi backend hoàn thiện. Hiện bạn có thể xác nhận đăng nhập và điều hướng menu.");
        setScreenStatus("Module kho — đang chờ triển khai backend.");
    }
}
