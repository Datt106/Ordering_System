package com.orderingsystem.fx.presentation.overseas;

import com.orderingsystem.uc004.boundary.dto.SiteDto;
import com.orderingsystem.fx.presentation.BaseViewController;
import com.orderingsystem.fx.presentation.StatusLabels;
import com.orderingsystem.fx.presentation.UiTasks;
import com.orderingsystem.fx.presentation.ux.FormValidation;
import com.orderingsystem.fx.presentation.ux.TableColumnLayout;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.List;

public class OverseasSitesController extends BaseViewController {

    /** Mã 12% · Tên 28% · Vận chuyển 50% · Trạng thái 10% */
    private static final double[] SITES_COL_RATIOS = {0.12, 0.28, 0.50, 0.10};

    @FXML
    private TableView<SiteDto> table;
    @FXML
    private TableColumn<SiteDto, String> codeCol;
    @FXML
    private TableColumn<SiteDto, String> nameCol;
    @FXML
    private TableColumn<SiteDto, String> shipCol;
    @FXML
    private TableColumn<SiteDto, String> statusCol;
    @FXML
    private TextField codeField;
    @FXML
    private TextField nameField;
    @FXML
    private TextArea otherArea;
    @FXML
    private Button addButton;

    @Override
    protected void onInit() {
        codeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().siteCode()));
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().siteName()));
        shipCol.setCellValueFactory(c -> new SimpleStringProperty(formatShipping(c.getValue())));
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().active() ? "Hoạt động" : "Ngừng"));

        TableColumnLayout.bindProportionalColumns(table, SITES_COL_RATIOS, codeCol, nameCol, shipCol, statusCol);
        TableColumnLayout.bindEllipsisCellFactory(nameCol);
        TableColumnLayout.bindEllipsisCellFactory(shipCol);

        table.getSelectionModel().selectedItemProperty().addListener((obs, o, row) -> {
            if (row != null) {
                codeField.setText(row.siteCode());
                nameField.setText(row.siteName());
                otherArea.setText(row.otherInfo() != null ? row.otherInfo() : "");
            }
        });
        FormValidation.bindDisabledUntilFilled(addButton, codeField, nameField);
        bindEmptyTable(table, "Chưa có Site — thêm mã Site và tên đối tác bên dưới.");
        refresh();
    }

    @FXML
    private void onRefresh() {
        refresh();
    }

    @FXML
    private void onAdd() {
        try {
            validateRequired(codeField, "Nhập mã Site (*).");
            validateRequired(nameField, "Nhập tên Site (*).");
        } catch (IllegalArgumentException ex) {
            UiTasks.showError(ex);
            return;
        }
        String code = codeField.getText().trim();
        String name = nameField.getText().trim();
        String other = otherArea.getText();
        UiTasks.runWithStatus(
                "Đang đăng ký Site…",
                () -> {
                    app.uc004().registerSite(code, name, other);
                    return code;
                },
                savedCode -> {
                    UiTasks.showInfo(
                            "Đã lưu",
                            "Site " + savedCode + " đã được thêm.\n"
                                    + "Đại diện Site dùng màn Đăng ký tài khoản (từ màn đăng nhập) để tạo user gắn mã này.");
                    refresh();
                },
                "Danh sách Site đã cập nhật."
        );
    }

    @FXML
    private void onUpdate() {
        try {
            validateRequired(codeField, "Chọn Site trong bảng (*).");
            validateRequired(nameField, "Nhập tên Site (*).");
        } catch (IllegalArgumentException ex) {
            UiTasks.showError(ex);
            return;
        }
        String code = codeField.getText().trim();
        String name = nameField.getText().trim();
        String other = otherArea.getText();
        UiTasks.runWithStatus(
                "Đang cập nhật…",
                () -> {
                    app.uc004().updateMaster(code, name, other);
                    return code;
                },
                savedCode -> {
                    UiTasks.showInfo("Đã lưu", "Đã cập nhật Site " + savedCode);
                    refresh();
                },
                "Danh sách Site đã cập nhật."
        );
    }

    @FXML
    private void onDelete() {
        try {
            validateRequired(codeField, "Chọn Site cần xóa.");
        } catch (IllegalArgumentException ex) {
            UiTasks.showError(ex);
            return;
        }
        String code = codeField.getText().trim();
        if (!UiTasks.confirmDelete("Site: " + code)) {
            setScreenStatus("Đã hủy xóa Site.");
            return;
        }
        UiTasks.runWithStatus(
                "Đang xóa Site…",
                () -> {
                    app.uc004().deleteSite(code);
                    return code;
                },
                deletedCode -> {
                    UiTasks.showInfo("Đã xóa", "Site " + deletedCode + " đã được gỡ.");
                    refresh();
                },
                "Danh sách Site đã cập nhật."
        );
    }

    private void refresh() {
        UiTasks.runWithStatus(
                "Đang tải Site…",
                () -> app.uc004().listAllSites(),
                this::applySiteList,
                "Danh sách Site sẵn sàng."
        );
    }

    private void applySiteList(List<SiteDto> items) {
        table.setItems(FXCollections.observableArrayList(items));
        setScreenStatus("Có " + items.size() + " Site.");
    }

    private static String formatShipping(SiteDto site) {
        if (site.shipDays() == null || site.airDays() == null) {
            return StatusLabels.shippingStatus(site.shippingStatus());
        }
        return "Tàu " + site.shipDays() + " ngày / Bay " + site.airDays() + " ngày";
    }
}
