package com.orderingsystem.fx.presentation.warehouse;

import com.orderingsystem.core.domain.OrderStatus;
import com.orderingsystem.uc013.boundary.dto.WarehouseOrderDto;
import com.orderingsystem.fx.presentation.BaseViewController;
import com.orderingsystem.fx.presentation.StatusLabels;
import com.orderingsystem.fx.presentation.ux.TableColumnLayout;
import com.orderingsystem.fx.presentation.UiTasks;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class WarehouseOrdersController extends BaseViewController {

    // Tỷ lệ mới tối ưu: Mã đơn (16%), Site (24%), Hàng (24%), Đặt (7%), Nhận (7%), Thời gian (12%), Trạng thái (10%)
    private static final double[] COL_RATIOS = {0.16, 0.24, 0.24, 0.07, 0.07, 0.12, 0.10};

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.systemDefault());

    @FXML private ComboBox<OrderStatus> statusFilter;
    @FXML private TextField siteFilter;
    @FXML private TextField merchandiseFilter;
    @FXML private VBox resultCard;
    @FXML private HBox tableArea;
    @FXML private VBox tableContainer;
    @FXML private TableView<WarehouseOrderDto> table;
    
    @FXML private TableColumn<WarehouseOrderDto, String> orderIdCol;
    @FXML private TableColumn<WarehouseOrderDto, String> siteCol;
    @FXML private TableColumn<WarehouseOrderDto, String> codeCol;
    @FXML private TableColumn<WarehouseOrderDto, String> qtyCol;
    @FXML private TableColumn<WarehouseOrderDto, String> actualQtyCol;
    @FXML private TableColumn<WarehouseOrderDto, String> timeCol;
    @FXML private TableColumn<WarehouseOrderDto, String> statusCol;

    @Override
    protected void onInit() {
        statusFilter.setItems(FXCollections.observableArrayList(
                null, OrderStatus.CHO_GUI, OrderStatus.DA_GUI, OrderStatus.DA_XAC_NHAN,
                OrderStatus.TU_CHOI, OrderStatus.DA_NHAP_KHO, OrderStatus.SAI_LECH
        ));
        statusFilter.setConverter(orderStatusConverter());
        statusFilter.getSelectionModel().selectFirst();

        orderIdCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().orderId()));
        
        // Nối Mã + Tên
        siteCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().siteCode() + (c.getValue().siteName() != null ? " - " + c.getValue().siteName() : "")));
        codeCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().merchandiseCode() + (c.getValue().merchandiseName() != null ? " - " + c.getValue().merchandiseName() : "")));
        
        qtyCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().quantityOrdered())));
        actualQtyCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().actualQuantity() != null ? String.valueOf(c.getValue().actualQuantity()) : "-"
        ));
        timeCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().reconciledAt() != null ? TIME_FORMATTER.format(c.getValue().reconciledAt()) : "-"
        ));
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(StatusLabels.orderStatus(c.getValue().status())));

        TableColumnLayout.bindProportionalColumns(table, COL_RATIOS, 
                orderIdCol, siteCol, codeCol, qtyCol, actualQtyCol, timeCol, statusCol);
        
        TableColumnLayout.bindEllipsisCellFactory(orderIdCol);
        TableColumnLayout.bindEllipsisCellFactory(siteCol);
        TableColumnLayout.bindEllipsisCellFactory(codeCol);
        TableColumnLayout.bindEllipsisCellFactory(statusCol);

        bindResultTableInsets(tableContainer, tableArea, 0.02);
        TableColumnLayout.applyRowHeight(table);
        Platform.runLater(this::onSearch);
    }

    @FXML
    private void onSearch() {
        OrderStatus status = statusFilter.getValue();
        String site = siteFilter.getText();
        String merchandise = merchandiseFilter.getText();
        UiTasks.<List<WarehouseOrderDto>>runWithStatus(
                "Đang tìm...",
                () -> app.uc013().listOrders(status, site, merchandise),
                this::applyOrders,
                "Danh sách đã cập nhật."
        );
    }

    private void applyOrders(List<WarehouseOrderDto> items) {
        table.setItems(FXCollections.observableArrayList(items));
        setScreenStatus(items.isEmpty() ? "Không có đơn khớp bộ lọc." : "Tìm thấy " + items.size() + " đơn.");
    }

    private static StringConverter<OrderStatus> orderStatusConverter() {
        return new StringConverter<>() {
            @Override public String toString(OrderStatus status) {
                return status == null ? "Tất cả trạng thái" : StatusLabels.orderStatus(status);
            }
            @Override public OrderStatus fromString(String string) { return null; }
        };
    }
}