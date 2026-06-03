package com.orderingsystem.fx.presentation.overseas;

import com.orderingsystem.uc004.boundary.dto.SiteDto;
import com.orderingsystem.fx.presentation.BaseViewController;
import com.orderingsystem.fx.presentation.StatusLabels;
import com.orderingsystem.fx.presentation.UiTasks;
import com.orderingsystem.fx.presentation.ux.FormPanels;
import com.orderingsystem.fx.presentation.ux.FormValidation;
import com.orderingsystem.fx.presentation.ux.TableColumnLayout;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.List;

public class OverseasSitesController extends BaseViewController {

    private static final double[] SITES_COL_RATIOS = {0.12, 0.22, 0.24, 0.32, 0.10};

    @FXML
    private TableView<SiteDto> table;
    @FXML
    private VBox tableContainer;
    @FXML
    private TableColumn<SiteDto, String> codeCol;
    @FXML
    private TableColumn<SiteDto, String> nameCol;
    @FXML
    private TableColumn<SiteDto, String> otherCol;
    @FXML
    private TableColumn<SiteDto, String> shipCol;
    @FXML
    private TableColumn<SiteDto, String> statusCol;
    @FXML
    private VBox formPanel;
    @FXML
    private Label formTitleLabel;
    @FXML
    private TextField codeField;
    @FXML
    private TextField nameField;
    @FXML
    private TextArea otherArea;
    @FXML
    private Button updateButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button formOkButton;

    private FormPanels.Mode formMode;

    @Override
    protected void onInit() {
        codeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().siteCode()));
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().siteName()));
        otherCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().otherInfo() != null && !c.getValue().otherInfo().isBlank()
                        ? c.getValue().otherInfo()
                        : "—"));
        shipCol.setCellValueFactory(c -> new SimpleStringProperty(formatShipping(c.getValue())));
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().active() ? "Hoạt động" : "Ngừng"));

        TableColumnLayout.bindProportionalColumns(table, SITES_COL_RATIOS, codeCol, nameCol, otherCol, shipCol, statusCol);
        TableColumnLayout.bindEllipsisCellFactory(nameCol);
        TableColumnLayout.bindEllipsisCellFactory(otherCol);
        TableColumnLayout.bindEllipsisCellFactory(shipCol);
        bindTableScroll(table, tableContainer);

        FormValidation.bindDisabledUntilTableSelection(updateButton, table);
        FormValidation.bindDisabledUntilTableSelection(deleteButton, table);
        FormValidation.bindDisabledUntilFilled(formOkButton, codeField, nameField);
        refresh();
    }

    @FXML
    private void onRefresh() {
        refresh();
    }

    @FXML
    private void onShowAddForm() {
        formMode = FormPanels.Mode.ADD;
        formTitleLabel.setText("Thêm Site");
        clearFormFields(true);
        FormPanels.open(formPanel);
    }

    @FXML
    private void onShowEditForm() {
        SiteDto selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiTasks.showError(new IllegalArgumentException("Chọn Site trong bảng để sửa."));
            return;
        }
        formMode = FormPanels.Mode.EDIT;
        formTitleLabel.setText("Sửa Site");
        fillForm(selected);
        FormPanels.open(formPanel);
    }

    @FXML
    private void onFormOk() {
        if (formMode == FormPanels.Mode.ADD) {
            performAdd();
        } else {
            performUpdate();
        }
    }

    @FXML
    private void onFormClose() {
        FormPanels.close(formPanel);
    }

    @FXML
    private void onDelete() {
        SiteDto selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiTasks.showError(new IllegalArgumentException("Chọn Site cần xóa trong bảng."));
            return;
        }
        String code = selected.siteCode();
        if (!UiTasks.confirmDelete("Site: " + code)) {
            return;
        }
        UiTasks.runWithStatus(
                "Đang xóa Site…",
                () -> {
                    app.uc004().deleteSite(code);
                    return code;
                },
                deletedCode -> {
                    FormPanels.close(formPanel);
                    UiTasks.showInfo("Đã xóa", "Site " + deletedCode + " đã được gỡ.");
                    refresh();
                },
                "Danh sách Site đã cập nhật."
        );
    }

    private void performAdd() {
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
                    FormPanels.close(formPanel);
                    UiTasks.showInfo(
                            "Đã lưu",
                            "Site " + savedCode + " đã được thêm.\n"
                                    + "Đại diện Site dùng màn Đăng ký tài khoản (từ màn đăng nhập) để tạo user gắn mã này.");
                    refresh();
                },
                "Danh sách Site đã cập nhật."
        );
    }

    private void performUpdate() {
        SiteDto selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiTasks.showError(new IllegalArgumentException("Chọn Site trong bảng để cập nhật."));
            return;
        }
        try {
            validateRequired(nameField, "Nhập tên Site (*).");
        } catch (IllegalArgumentException ex) {
            UiTasks.showError(ex);
            return;
        }
        String code = selected.siteCode();
        String name = nameField.getText().trim();
        String other = otherArea.getText();
        UiTasks.runWithStatus(
                "Đang cập nhật…",
                () -> {
                    app.uc004().updateMaster(code, name, other);
                    return code;
                },
                savedCode -> {
                    FormPanels.close(formPanel);
                    UiTasks.showInfo("Đã lưu", "Đã cập nhật Site " + savedCode);
                    refresh();
                },
                "Danh sách Site đã cập nhật."
        );
    }

    private void refresh() {
        UiTasks.runWithStatus(
                "Đang tải Site…",
                () -> app.uc004().listAllSites(),
                items -> table.setItems(FXCollections.observableArrayList(items)),
                "Danh sách Site sẵn sàng."
        );
    }

    private void fillForm(SiteDto row) {
        codeField.setText(row.siteCode());
        codeField.setEditable(false);
        nameField.setText(row.siteName());
        otherArea.setText(row.otherInfo() != null ? row.otherInfo() : "");
    }

    private void clearFormFields(boolean editableCode) {
        codeField.clear();
        codeField.setEditable(editableCode);
        nameField.clear();
        otherArea.clear();
    }

    private static String formatShipping(SiteDto site) {
        if (site.shipDays() == null || site.airDays() == null) {
            return StatusLabels.shippingStatus(site.shippingStatus());
        }
        return "Tàu " + site.shipDays() + " ngày / Bay " + site.airDays() + " ngày";
    }
}
