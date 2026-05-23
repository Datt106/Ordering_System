package com.orderingsystem.fx.presentation.overseas;

import com.orderingsystem.uc004.dto.SiteDto;
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
        UiTasks.runWithStatus(
                "Đang đăng ký Site…",
                () -> {
                    validateRequired(codeField, "Nhập mã Site (*).");
                    validateRequired(nameField, "Nhập tên Site (*).");
                    app.sites().registerSite(codeField.getText(), nameField.getText(), otherArea.getText());
                    UiTasks.showInfo("Đã lưu", "Site " + codeField.getText().trim() + " đã được đăng ký.");
                    refresh();
                },
                "Danh sách Site đã cập nhật."
        );
    }

    @FXML
    private void onUpdate() {
        UiTasks.runWithStatus(
                "Đang cập nhật…",
                () -> {
                    validateRequired(codeField, "Chọn Site trong bảng (*).");
                    validateRequired(nameField, "Nhập tên Site (*).");
                    app.sites().updateMaster(codeField.getText(), nameField.getText(), otherArea.getText());
                    UiTasks.showInfo("Đã lưu", "Đã cập nhật Site " + codeField.getText().trim());
                    refresh();
                },
                "Danh sách Site đã cập nhật."
        );
    }

    @FXML
    private void onDelete() {
        validateRequired(codeField, "Chọn Site cần xóa.");
        String code = codeField.getText().trim();
        if (!UiTasks.confirmDelete("Site: " + code)) {
            setScreenStatus("Đã hủy xóa Site.");
            return;
        }
        UiTasks.runWithStatus(
                "Đang xóa Site…",
                () -> {
                    app.sites().deleteSite(code);
                    UiTasks.showInfo("Đã xóa", "Site " + code + " đã được gỡ.");
                    refresh();
                },
                "Danh sách Site đã cập nhật."
        );
    }

    private void refresh() {
        UiTasks.runWithStatus(
                "Đang tải Site…",
                () -> {
                    var items = app.sites().listAllSites();
                    table.setItems(FXCollections.observableArrayList(items));
                    setScreenStatus("Có " + items.size() + " Site.");
                },
                "Danh sách Site sẵn sàng."
        );
    }

    private static String formatShipping(SiteDto site) {
        if (site.shipDays() == null || site.airDays() == null) {
            return StatusLabels.shippingStatus(site.shippingStatus());
        }
        return "Tàu " + site.shipDays() + " ngày / Bay " + site.airDays() + " ngày";
    }
}
