package com.orderingsystem.fx.presentation.site;

import com.orderingsystem.uc004.dto.SiteDto;
import com.orderingsystem.fx.presentation.BaseViewController;
import com.orderingsystem.fx.presentation.StatusLabels;
import com.orderingsystem.fx.presentation.UiTasks;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;

public class SiteShippingController extends BaseViewController {

    @FXML
    private Label siteCodeLabel;
    @FXML
    private Label siteNameLabel;
    @FXML
    private Label shippingStatusLabel;
    @FXML
    private Spinner<Integer> shipSpinner;
    @FXML
    private Spinner<Integer> airSpinner;

    @Override
    protected void onInit() {
        shipSpinner.setValueFactory(new javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory(1, 365, 30));
        airSpinner.setValueFactory(new javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory(1, 90, 7));
        loadSite();
    }

    @FXML
    private void onRefresh() {
        loadSite();
    }

    @FXML
    private void onSave() {
        UiTasks.runWithStatus(
                "Đang lưu…",
                () -> {
                    SiteDto updated = app.siteShipping().updateMyShipping(shipSpinner.getValue(), airSpinner.getValue());
                    bindSite(updated);
                    UiTasks.showInfo(
                            "Đã lưu vận chuyển",
                            "Tàu: " + updated.shipDays() + " ngày · Bay: " + updated.airDays() + " ngày."
                    );
                },
                "Thông tin vận chuyển đã cập nhật."
        );
    }

    private void loadSite() {
        UiTasks.runWithStatus(
                "Đang tải…",
                () -> bindSite(app.siteShipping().getMySite()),
                "Sẵn sàng."
        );
    }

    private void bindSite(SiteDto site) {
        siteCodeLabel.setText(site.siteCode());
        siteNameLabel.setText(site.siteName());
        shippingStatusLabel.setText(StatusLabels.shippingStatus(site.shippingStatus()));
        if (site.shipDays() != null) {
            shipSpinner.getValueFactory().setValue(site.shipDays());
        }
        if (site.airDays() != null) {
            airSpinner.getValueFactory().setValue(site.airDays());
        }
        setScreenStatus(site.shippingStatus() == com.orderingsystem.domain.site.ShippingStatus.DA_KHAI_BAO
                ? "Site đã khai báo vận chuyển — có thể nhận truy vấn tồn kho."
                : "Lưu số ngày tàu/bay để kích hoạt truy vấn tồn kho.");
    }
}
