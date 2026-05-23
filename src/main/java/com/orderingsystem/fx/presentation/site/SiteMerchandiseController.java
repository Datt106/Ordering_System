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

import java.util.List;

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
        loadCatalog();
        refresh();
    }

    private void loadCatalog() {
        UiTasks.runWithStatus(
                "Đang tải danh mục…",
                () -> app.catalog().listCatalogForBrowsing(),
                this::applyCatalogCodes,
                "Danh mục chuẩn sẵn sàng."
        );
    }

    private void applyCatalogCodes(List<StandardMerchandiseDto> catalog) {
        catalogCombo.setItems(FXCollections.observableArrayList(
                catalog.stream().map(StandardMerchandiseDto::merchandiseCode).toList()));
        if (!catalog.isEmpty()) {
            catalogCombo.getSelectionModel().selectFirst();
        }
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
                    return code;
                },
                addedCode -> {
                    setScreenStatus("Đã thêm " + addedCode);
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
        String code = selected.merchandiseCode();
        UiTasks.runWithStatus(
                "Đang xóa…",
                () -> {
                    app.siteMerchandise().removeMerchandise(code);
                    return code;
                },
                removedCode -> {
                    setScreenStatus("Đã xóa " + removedCode);
                    refresh();
                },
                "Danh sách đã cập nhật."
        );
    }

    private void refresh() {
        UiTasks.runWithStatus(
                "Đang tải…",
                () -> app.siteMerchandise().listMyMerchandise(),
                this::applyMerchandiseList,
                "Sẵn sàng."
        );
    }

    private void applyMerchandiseList(List<SiteMerchandiseDto> items) {
        table.setItems(FXCollections.observableArrayList(items));
        setScreenStatus("Site đang kinh doanh " + items.size() + " mặt hàng.");
    }
}
