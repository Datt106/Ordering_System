package com.orderingsystem.fx.presentation.site;

import com.orderingsystem.uc001.boundary.dto.StandardMerchandiseDto;
import com.orderingsystem.uc009.boundary.dto.SiteMerchandiseDto;
import com.orderingsystem.fx.presentation.BaseViewController;
import com.orderingsystem.fx.presentation.UiTasks;
import com.orderingsystem.fx.presentation.ux.FormPanels;
import com.orderingsystem.fx.presentation.ux.MerchandisePicker;
import com.orderingsystem.fx.presentation.ux.TableColumnLayout;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

import java.util.List;

public class SiteMerchandiseController extends BaseViewController {

    private static final double[] MERCHANDISE_COL_RATIOS = {0.20, 0.80};

    @FXML
    private TableView<SiteMerchandiseDto> table;
    @FXML
    private VBox tableContainer;
    @FXML
    private TableColumn<SiteMerchandiseDto, String> codeCol;
    @FXML
    private TableColumn<SiteMerchandiseDto, String> nameCol;
    @FXML
    private VBox formPanel;
    @FXML
    private ComboBox<String> catalogCombo;

    @Override
    protected void onInit() {
        codeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().merchandiseCode()));
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().merchandiseName() != null ? c.getValue().merchandiseName() : ""));

        TableColumnLayout.bindProportionalColumns(table, MERCHANDISE_COL_RATIOS, codeCol, nameCol);
        TableColumnLayout.bindEllipsisCellFactory(nameCol);
        bindTableScroll(table, tableContainer);
        loadCatalog();
        refresh();
    }

    @FXML
    private void onShowAddForm() {
        if (!catalogCombo.getItems().isEmpty()) {
            catalogCombo.getSelectionModel().selectFirst();
        }
        FormPanels.open(formPanel);
    }

    @FXML
    private void onFormOk() {
        String code = MerchandisePicker.extractCode(catalogCombo.getEditor().getText());
        if (code.isBlank()) {
            code = MerchandisePicker.extractCode(catalogCombo.getValue());
        }
        if (code.isBlank()) {
            UiTasks.showError(new IllegalArgumentException("Chọn mã hàng từ danh mục chuẩn."));
            return;
        }
        String merchandiseCode = code;
        UiTasks.runWithStatus(
                "Đang thêm…",
                () -> {
                    app.uc009().addMerchandise(merchandiseCode);
                    return merchandiseCode;
                },
                addedCode -> {
                    FormPanels.close(formPanel);
                    refresh();
                },
                "Danh sách đã cập nhật."
        );
    }

    @FXML
    private void onFormClose() {
        FormPanels.close(formPanel);
    }

    private void loadCatalog() {
        UiTasks.runWithStatus(
                "Đang tải danh mục…",
                () -> app.uc001().listCatalogForBrowsing(),
                this::applyCatalogCodes,
                "Danh mục chuẩn sẵn sàng."
        );
    }

    private void applyCatalogCodes(List<StandardMerchandiseDto> catalog) {
        MerchandisePicker.bindCatalog(catalogCombo, catalog);
    }

    @FXML
    private void onRefresh() {
        refresh();
    }

    @FXML
    private void onRemove() {
        SiteMerchandiseDto selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiTasks.showError(new IllegalArgumentException("Chọn mặt hàng cần xóa trong bảng."));
            return;
        }
        if (!UiTasks.confirmDelete("Mặt hàng kinh doanh: " + selected.merchandiseCode())) {
            return;
        }
        String code = selected.merchandiseCode();
        UiTasks.runWithStatus(
                "Đang xóa…",
                () -> {
                    app.uc009().removeMerchandise(code);
                    return code;
                },
                removedCode -> refresh(),
                "Danh sách đã cập nhật."
        );
    }

    private void refresh() {
        UiTasks.runWithStatus(
                "Đang tải…",
                () -> app.uc009().listMyMerchandise(),
                items -> table.setItems(FXCollections.observableArrayList(items)),
                "Sẵn sàng."
        );
    }
}
