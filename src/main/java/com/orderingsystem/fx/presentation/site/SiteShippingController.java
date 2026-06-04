package com.orderingsystem.fx.presentation.site;

import com.orderingsystem.core.domain.ShippingStatus;
import com.orderingsystem.uc004.boundary.dto.SiteDto;
import com.orderingsystem.fx.presentation.BaseViewController;
import com.orderingsystem.fx.presentation.StatusLabels;
import com.orderingsystem.fx.presentation.UiTasks;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class SiteShippingController extends BaseViewController {

    @FXML private Label siteCodeLabel;
    @FXML private Label siteNameLabel;
    @FXML private Label shippingStatusLabel;
    
    // Đã thay đổi từ Spinner sang TextField
    @FXML private TextField shipField;
    @FXML private TextField airField;

    @Override
    protected void onInit() {
        loadSite();
    }

    @FXML
    private void onRefresh() {
        loadSite();
    }

    @FXML
    private void onSave() {
        try {
            int shipDays = Integer.parseInt(shipField.getText().trim());
            int airDays = Integer.parseInt(airField.getText().trim());
            UiTasks.<SiteDto>runWithStatus(
                    "Đang lưu...",
                    () -> app.uc010().updateMyShipping(shipDays, airDays),
                    this::bindSiteAfterSave,
                    "Thông tin vận chuyển đã cập nhật."
            );
        } catch (NumberFormatException e) {
            UiTasks.showError(new IllegalArgumentException("Vui lòng nhập số hợp lệ cho ngày tàu/bay."));
        }
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
            shipField.setText(String.valueOf(site.shipDays()));
        }
        if (site.airDays() != null) {
            airField.setText(String.valueOf(site.airDays()));
        }
        boolean declared = site.shippingStatus() == ShippingStatus.DA_KHAI_BAO;
        setScreenStatus(declared
                ? "Site đã khai báo vận chuyển - có thể nhận truy vấn tồn kho."
                : "Lưu số ngày tàu/bay để kích hoạt truy vấn tồn kho.");
    }
}