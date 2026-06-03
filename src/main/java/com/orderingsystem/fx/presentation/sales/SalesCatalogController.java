package com.orderingsystem.fx.presentation.sales;

import com.orderingsystem.uc001.boundary.dto.StandardMerchandiseDto;
import com.orderingsystem.fx.presentation.BaseViewController;
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

public class SalesCatalogController extends BaseViewController {

    private static final double[] CATALOG_COL_RATIOS = {0.15, 0.35, 0.50};

    @FXML
    private TableView<StandardMerchandiseDto> table;
    @FXML
    private VBox tableContainer;
    @FXML
    private TableColumn<StandardMerchandiseDto, String> codeCol;
    @FXML
    private TableColumn<StandardMerchandiseDto, String> nameCol;
    @FXML
    private TableColumn<StandardMerchandiseDto, String> descCol;
    @FXML
    private VBox formPanel;
    @FXML
    private Label formTitleLabel;
    @FXML
    private TextField codeField;
    @FXML
    private TextField nameField;
    @FXML
    private TextArea descArea;
    @FXML
    private Button updateButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button formOkButton;

    private FormPanels.Mode formMode;

    @Override
    protected void onInit() {
        codeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().merchandiseCode()));
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().merchandiseName()));
        descCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().description() != null ? c.getValue().description() : ""));

        TableColumnLayout.bindProportionalColumns(table, CATALOG_COL_RATIOS, codeCol, nameCol, descCol);
        TableColumnLayout.bindEllipsisCellFactory(nameCol);
        TableColumnLayout.bindEllipsisCellFactory(descCol);
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
        formTitleLabel.setText("Thêm mặt hàng");
        clearFormFields(true);
        FormPanels.open(formPanel);
    }

    @FXML
    private void onShowEditForm() {
        StandardMerchandiseDto selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiTasks.showError(new IllegalArgumentException("Chọn một dòng trong bảng để sửa."));
            return;
        }
        formMode = FormPanels.Mode.EDIT;
        formTitleLabel.setText("Sửa mặt hàng");
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
        StandardMerchandiseDto selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiTasks.showError(new IllegalArgumentException("Chọn mặt hàng cần xóa trong bảng."));
            return;
        }
        String code = selected.merchandiseCode();
        if (!UiTasks.confirmDelete("Mặt hàng: " + code)) {
            return;
        }
        UiTasks.runWithStatus(
                "Đang xóa…",
                () -> {
                    app.uc001().deleteMerchandise(code);
                    return code;
                },
                deletedCode -> {
                    FormPanels.close(formPanel);
                    UiTasks.showInfo("Đã xóa", "Mã " + deletedCode + " đã được gỡ khỏi danh mục.");
                    refresh();
                },
                "Danh mục đã cập nhật."
        );
    }

    private void performAdd() {
        try {
            validateRequired(codeField, "Nhập mã hàng (*).");
            validateRequired(nameField, "Nhập tên mặt hàng (*).");
        } catch (IllegalArgumentException ex) {
            UiTasks.showError(ex);
            return;
        }
        String code = codeField.getText().trim();
        String name = nameField.getText().trim();
        String desc = descArea.getText();
        UiTasks.runWithStatus(
                "Đang thêm mặt hàng…",
                () -> {
                    app.uc001().registerMerchandise(code, name, desc);
                    return code;
                },
                savedCode -> {
                    FormPanels.close(formPanel);
                    UiTasks.showInfo("Đã lưu", "Mặt hàng mới có trong danh mục chuẩn.");
                    refresh();
                },
                "Danh mục đã cập nhật."
        );
    }

    private void performUpdate() {
        StandardMerchandiseDto selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiTasks.showError(new IllegalArgumentException("Chọn một dòng trong bảng để cập nhật."));
            return;
        }
        try {
            validateRequired(nameField, "Nhập tên mặt hàng (*).");
        } catch (IllegalArgumentException ex) {
            UiTasks.showError(ex);
            return;
        }
        String code = selected.merchandiseCode();
        String name = nameField.getText().trim();
        String desc = descArea.getText();
        UiTasks.runWithStatus(
                "Đang cập nhật…",
                () -> {
                    app.uc001().updateMerchandise(code, name, desc);
                    return code;
                },
                savedCode -> {
                    FormPanels.close(formPanel);
                    UiTasks.showInfo("Đã lưu", "Thông tin mặt hàng đã được cập nhật.");
                    refresh();
                },
                "Danh mục đã cập nhật."
        );
    }

    private void refresh() {
        UiTasks.runWithStatus(
                "Đang tải danh mục…",
                () -> app.uc001().listAll(),
                items -> table.setItems(FXCollections.observableArrayList(items)),
                "Danh mục đã tải."
        );
    }

    private void fillForm(StandardMerchandiseDto row) {
        codeField.setText(row.merchandiseCode());
        codeField.setEditable(false);
        nameField.setText(row.merchandiseName());
        descArea.setText(row.description() != null ? row.description() : "");
    }

    private void clearFormFields(boolean editableCode) {
        codeField.clear();
        codeField.setEditable(editableCode);
        nameField.clear();
        descArea.clear();
    }
}
