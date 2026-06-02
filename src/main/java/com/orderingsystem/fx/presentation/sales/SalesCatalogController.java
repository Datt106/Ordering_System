package com.orderingsystem.fx.presentation.sales;

import com.orderingsystem.uc001.boundary.dto.StandardMerchandiseDto;
import com.orderingsystem.fx.presentation.BaseViewController;
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

public class SalesCatalogController extends BaseViewController {

    private static final double[] CATALOG_COL_RATIOS = {0.15, 0.35, 0.50};

    @FXML
    private TableView<StandardMerchandiseDto> table;
    @FXML
    private TableColumn<StandardMerchandiseDto, String> codeCol;
    @FXML
    private TableColumn<StandardMerchandiseDto, String> nameCol;
    @FXML
    private TableColumn<StandardMerchandiseDto, String> descCol;
    @FXML
    private TextField codeField;
    @FXML
    private TextField nameField;
    @FXML
    private TextArea descArea;
    @FXML
    private Button addButton;
    @FXML
    private Button updateButton;
    @FXML
    private Button deleteButton;

    private boolean fillingFormFromTable;

    @Override
    protected void onInit() {
        codeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().merchandiseCode()));
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().merchandiseName()));
        descCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().description() != null ? c.getValue().description() : ""));

        TableColumnLayout.bindProportionalColumns(table, CATALOG_COL_RATIOS, codeCol, nameCol, descCol);
        TableColumnLayout.bindEllipsisCellFactory(nameCol);
        TableColumnLayout.bindEllipsisCellFactory(descCol);

        table.getSelectionModel().selectedItemProperty().addListener((obs, o, row) -> {
            if (fillingFormFromTable) {
                return;
            }
            if (row != null) {
                fillForm(row);
                codeField.setEditable(false);
                setScreenStatus("Đang sửa: " + row.merchandiseCode());
            }
        });
        FormValidation.bindDisabledUntilFilled(addButton, codeField, nameField);
        FormValidation.bindDisabledUntilTableSelection(updateButton, table);
        FormValidation.bindDisabledUntilTableSelection(deleteButton, table);
        bindEmptyTable(table, "Chưa có mặt hàng trong danh mục. Dùng form bên dưới để thêm mã mới.");
        refresh();
    }

    @FXML
    private void onRefresh() {
        refresh();
    }

    @FXML
    private void onClearForm() {
        clearFormForNewEntry();
        setScreenStatus("Nhập mã và tên mới, rồi bấm Thêm.");
    }

    @FXML
    private void onAdd() {
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
                    setScreenStatus("Đã thêm: " + savedCode);
                    UiTasks.showInfo("Đã lưu", "Mặt hàng mới có trong danh mục chuẩn.");
                    refresh();
                },
                "Danh mục đã cập nhật."
        );
    }

    @FXML
    private void onUpdate() {
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
                    setScreenStatus("Đã cập nhật: " + savedCode);
                    UiTasks.showInfo("Đã lưu", "Thông tin mặt hàng đã được cập nhật.");
                    refresh();
                },
                "Danh mục đã cập nhật."
        );
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
            setScreenStatus("Đã hủy xóa.");
            return;
        }
        UiTasks.runWithStatus(
                "Đang xóa…",
                () -> {
                    app.uc001().deleteMerchandise(code);
                    return code;
                },
                deletedCode -> {
                    clearFormForNewEntry();
                    setScreenStatus("Đã xóa: " + deletedCode);
                    UiTasks.showInfo("Đã xóa", "Mã " + deletedCode + " đã được gỡ khỏi danh mục.");
                    refresh();
                },
                "Danh mục đã cập nhật."
        );
    }

    private void refresh() {
        UiTasks.runWithStatus(
                "Đang tải danh mục…",
                () -> app.uc001().listAll(),
                this::applyCatalogList,
                "Danh mục đã tải."
        );
    }

    private void applyCatalogList(List<StandardMerchandiseDto> items) {
        table.setItems(FXCollections.observableArrayList(items));
        setScreenStatus(items.isEmpty()
                ? "Danh mục trống — thêm mặt hàng đầu tiên."
                : "Hiển thị " + items.size() + " mặt hàng.");
    }

    private void fillForm(StandardMerchandiseDto row) {
        fillingFormFromTable = true;
        try {
            codeField.setText(row.merchandiseCode());
            nameField.setText(row.merchandiseName());
            descArea.setText(row.description() != null ? row.description() : "");
        } finally {
            fillingFormFromTable = false;
        }
    }

    private void clearFormForNewEntry() {
        fillingFormFromTable = true;
        try {
            table.getSelectionModel().clearSelection();
            codeField.clear();
            codeField.setEditable(true);
            nameField.clear();
            descArea.clear();
        } finally {
            fillingFormFromTable = false;
        }
    }
}
