package com.orderingsystem.fx.presentation.site;

import com.orderingsystem.core.domain.ShippingStatus;
import com.orderingsystem.uc004.boundary.dto.SiteDto;
import com.orderingsystem.fx.presentation.BaseViewController;
import com.orderingsystem.fx.presentation.StatusLabels;
import com.orderingsystem.fx.presentation.UiTasks;
import com.orderingsystem.fx.presentation.ux.SpinnerInputs;
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
        SpinnerInputs.configureIntegerSpinner(shipSpinner, 1, 365, 30);
        SpinnerInputs.configureIntegerSpinner(airSpinner, 1, 90, 7);
        loadSite();
    }

    @FXML
    private void onRefresh() {
        loadSite();
    }

    @FXML
    private void onSave() {
        int shipDays = shipSpinner.getValue();
        int airDays = airSpinner.getValue();
        UiTasks.<SiteDto>runWithStatus(
                "Đang lưu...",
                () -> app.uc010().updateMyShipping(shipDays, airDays),
                this::bindSiteAfterSave,
                "Thông tin vận chuyển đã cập nhật."
        );
    }

    private void bindSiteAfterSave(SiteDto updated) {
        bindSite(updated);
        UiTasks.showInfo(
                "Đã lưu vận chuyển",
                "Tàu: " + updated.shipDays() + " ngày - Bay: " + updated.airDays() + " ngày."
        );
    }

    private void loadSite() {
        UiTasks.<SiteDto>runWithStatus(
                "Đang tải...",
                () -> app.uc010().getMySite(),
                this::bindSite,
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
        boolean declared = site.shippingStatus() == ShippingStatus.DA_KHAI_BAO;
        setScreenStatus(declared
                ? "Site đã khai báo vận chuyển - có thể nhận truy vấn tồn kho."
                : "Lưu số ngày tàu/bay để kích hoạt truy vấn tồn kho.");
    }
}
