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
    private Button deactivateButton;
    @FXML
    private Button activateButton;
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

        FormValidation.bindDisabledUntilFilled(formOkButton, codeField, nameField);

        table.getSelectionModel().selectedItemProperty().addListener((obs, o, row) -> updateActionButtons(row));
        refresh();
    }

    private void updateActionButtons(SiteDto row) {
        boolean hasRow = row != null;
        boolean active = hasRow && row.active();

        FormValidation.unbindDisable(deactivateButton);
        FormValidation.unbindDisable(activateButton);
        FormValidation.unbindDisable(deleteButton);

        deactivateButton.setDisable(!hasRow || !active);
        activateButton.setDisable(!hasRow || active);
        deleteButton.setDisable(!hasRow || active);
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
    private void onDeactivate() {
        SiteDto selected = requireSelectedSite();
        if (selected == null) {
            return;
        }
        if (!selected.active()) {
            UiTasks.showError(new IllegalStateException("Site đã ngừng hoạt động."));
            return;
        }
        String code = selected.siteCode();
        if (!UiTasks.confirm(
                "Ngừng hoạt động Site",
                "Ngừng hợp tác với Site " + code + "?",
                "Tài khoản Site sẽ không đăng nhập được. Site không dùng cho truy vấn tồn kho mới. Lịch sử đơn hàng được giữ."
        )) {
            return;
        }
        UiTasks.runWithStatus(
                "Đang cập nhật…",
                () -> app.uc004().deactivateSite(code),
                updated -> {
                    UiTasks.showInfo("Đã ngừng hoạt động", "Site " + updated.siteCode() + " đã chuyển sang Ngừng.");
                    refresh();
                },
                "Danh sách Site đã cập nhật."
        );
    }

    @FXML
    private void onActivate() {
        SiteDto selected = requireSelectedSite();
        if (selected == null) {
            return;
        }
        if (selected.active()) {
            UiTasks.showError(new IllegalStateException("Site đang hoạt động."));
            return;
        }
        String code = selected.siteCode();
        if (!UiTasks.confirm(
                "Kích hoạt lại Site",
                "Kích hoạt lại Site " + code + "?",
                "Site có thể nhận truy vấn tồn kho và tài khoản Site (nếu còn) có thể đăng nhập lại."
        )) {
            return;
        }
        UiTasks.runWithStatus(
                "Đang kích hoạt…",
                () -> app.uc004().activateSite(code),
                updated -> {
                    UiTasks.showInfo("Đã kích hoạt", "Site " + updated.siteCode() + " đang Hoạt động.");
                    refresh();
                },
                "Danh sách Site đã cập nhật."
        );
    }

    @FXML
    private void onDelete() {
        SiteDto selected = requireSelectedSite();
        if (selected == null) {
            return;
        }
        if (selected.active()) {
            UiTasks.showError(new IllegalStateException(
                    "Chỉ xóa Site đã ngừng hoạt động. Dùng \"Ngừng hoạt động\" trước."));
            return;
        }
        String code = selected.siteCode();
        if (!UiTasks.confirmDelete("Site (đã ngừng): " + code)) {
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
                    UiTasks.showInfo(
                            "Đã xóa",
                            "Site " + deletedCode + " đã gỡ khỏi hệ thống.\n"
                                    + "Mã Site có thể dùng lại khi thêm Site mới.");
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
                                    + "Mỗi Site chỉ một tài khoản — đại diện Site đăng ký từ màn đăng nhập.");
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
                items -> {
                    table.setItems(FXCollections.observableArrayList(items));
                    updateActionButtons(table.getSelectionModel().getSelectedItem());
                },
                "Danh sách Site sẵn sàng."
        );
    }

    private SiteDto requireSelectedSite() {
        SiteDto selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiTasks.showError(new IllegalArgumentException("Chọn Site trong bảng."));
            return null;
        }
        return selected;
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
