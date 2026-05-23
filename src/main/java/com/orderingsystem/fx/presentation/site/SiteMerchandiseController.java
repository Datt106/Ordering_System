package com.orderingsystem.fx.presentation.site;

import com.orderingsystem.uc001.dto.StandardMerchandiseDto;
import com.orderingsystem.uc009.dto.SiteMerchandiseDto;
import com.orderingsystem.fx.presentation.BaseViewController;
import com.orderingsystem.fx.presentation.UiTasks;
import com.orderingsystem.fx.presentation.ux.TableColumnLayout;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class SiteMerchandiseController extends BaseViewController {

    /** Mã 20% · Tên 80% */
    private static final double[] MERCHANDISE_COL_RATIOS = {0.20, 0.80};

    @FXML
    private TableView<SiteMerchandiseDto> table;
    @FXML
    private TableColumn<SiteMerchandiseDto, String> codeCol;
    @FXML
    private TableColumn<SiteMerchandiseDto, String> nameCol;
    @FXML
    private ComboBox<String> catalogCombo;

    @Override
    protected void onInit() {
        codeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().merchandiseCode()));
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().merchandiseName() != null ? c.getValue().merchandiseName() : ""));

        TableColumnLayout.bindProportionalColumns(table, MERCHANDISE_COL_RATIOS, codeCol, nameCol);
        TableColumnLayout.bindEllipsisCellFactory(nameCol);

        bindEmptyTable(table, "Chưa khai báo mặt hàng kinh doanh — chọn mã từ danh mục chuẩn và bấm Thêm.");
        UiTasks.runWithStatus(
                "Đang tải danh mục…",
                () -> {
                    var catalog = app.catalog().listCatalogForBrowsing();
                    catalogCombo.setItems(FXCollections.observableArrayList(
                            catalog.stream().map(StandardMerchandiseDto::merchandiseCode).toList()));
                    if (!catalog.isEmpty()) {
                        catalogCombo.getSelectionModel().selectFirst();
                    }
                },
                "Danh mục chuẩn sẵn sàng."
        );
        refresh();
    }

    @FXML
    private void onRefresh() {
        refresh();
    }

    @FXML
    private void onAdd() {
        String code = catalogCombo.getValue();
        if (code == null || code.isBlank()) {
            setScreenStatus("Chọn mã hàng từ danh mục chuẩn.");
            return;
        }
        UiTasks.runWithStatus(
                "Đang thêm…",
                () -> {
                    app.siteMerchandise().addMerchandise(code);
                    setScreenStatus("Đã thêm " + code);
                    refresh();
                },
                "Danh sách đã cập nhật."
        );
    }

    @FXML
    private void onRemove() {
        SiteMerchandiseDto selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setScreenStatus("Chọn mặt hàng cần xóa trong bảng.");
            return;
        }
        if (!UiTasks.confirmDelete("Mặt hàng kinh doanh: " + selected.merchandiseCode())) {
            setScreenStatus("Đã hủy xóa.");
            return;
        }
        UiTasks.runWithStatus(
                "Đang xóa…",
                () -> {
                    app.siteMerchandise().removeMerchandise(selected.merchandiseCode());
                    setScreenStatus("Đã xóa " + selected.merchandiseCode());
                    refresh();
                },
                "Danh sách đã cập nhật."
        );
    }

    private void refresh() {
        UiTasks.runWithStatus(
                "Đang tải…",
                () -> {
                    var items = app.siteMerchandise().listMyMerchandise();
                    table.setItems(FXCollections.observableArrayList(items));
                    setScreenStatus("Site đang kinh doanh " + items.size() + " mặt hàng.");
                },
                "Sẵn sàng."
        );
    }
}
