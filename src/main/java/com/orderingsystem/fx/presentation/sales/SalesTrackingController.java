package com.orderingsystem.fx.presentation.sales;

import com.orderingsystem.core.domain.RequestStatus;
import com.orderingsystem.uc003.boundary.dto.ImportRequestListItemDto;
import com.orderingsystem.uc003.boundary.dto.ImportRequestTrackingDetailDto;
import com.orderingsystem.uc003.boundary.dto.PurchaseOrderTrackingDto;
import com.orderingsystem.fx.presentation.BaseViewController;
import com.orderingsystem.fx.presentation.StatusLabels;
import com.orderingsystem.fx.presentation.UiTasks;
import com.orderingsystem.fx.presentation.ux.TableColumnLayout;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class SalesTrackingController extends BaseViewController {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.systemDefault());

    /** Mã YC 30% · Tạo lúc 25% · Dòng 10% · Trạng thái 35% */
    private static final double[] TRACKING_COL_RATIOS = {0.30, 0.25, 0.10, 0.35};
    private static final double[] CHILD_ORDER_COL_RATIOS = {0.14, 0.10, 0.12, 0.10, 0.10, 0.08, 0.14, 0.22};

    @FXML
    private ComboBox<RequestStatus> statusFilter;
    @FXML
    private DatePicker fromPicker;
    @FXML
    private DatePicker toPicker;
    @FXML
    private VBox tableContainer;
    @FXML
    private TableView<ImportRequestListItemDto> listTable;
    @FXML
    private TableColumn<ImportRequestListItemDto, String> idCol;
    @FXML
    private TableColumn<ImportRequestListItemDto, String> createdCol;
    @FXML
    private TableColumn<ImportRequestListItemDto, String> itemsCol;
    @FXML
    private TableColumn<ImportRequestListItemDto, String> statusCol;
    @FXML
    private TextArea detailArea;
    @FXML
    private VBox childOrdersSection;
    @FXML
    private VBox childOrdersContainer;
    @FXML
    private TableView<PurchaseOrderTrackingDto> childOrdersTable;
    @FXML
    private TableColumn<PurchaseOrderTrackingDto, String> coOrderCol;
    @FXML
    private TableColumn<PurchaseOrderTrackingDto, String> coSiteCol;
    @FXML
    private TableColumn<PurchaseOrderTrackingDto, String> coMerchCol;
    @FXML
    private TableColumn<PurchaseOrderTrackingDto, String> coQtyCol;
    @FXML
    private TableColumn<PurchaseOrderTrackingDto, String> coActualCol;
    @FXML
    private TableColumn<PurchaseOrderTrackingDto, String> coDiffCol;
    @FXML
    private TableColumn<PurchaseOrderTrackingDto, String> coShipCol;
    @FXML
    private TableColumn<PurchaseOrderTrackingDto, String> coStatusCol;

    @Override
    protected void onInit() {
        statusFilter.setItems(FXCollections.observableArrayList(
                null,
                RequestStatus.CHO_XU_LY,
                RequestStatus.DANG_XU_LY,
                RequestStatus.DA_TACH_DON,
                RequestStatus.LOI,
                RequestStatus.TU_CHOI
        ));
        statusFilter.setConverter(statusConverter());
        statusFilter.getSelectionModel().selectFirst();

        idCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().requestId()));
        createdCol.setCellValueFactory(c -> new SimpleStringProperty(formatInstant(c.getValue().createdAt())));
        itemsCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().itemCount())));
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(StatusLabels.requestStatus(c.getValue().status())));

        TableColumnLayout.bindProportionalColumns(listTable, TRACKING_COL_RATIOS, idCol, createdCol, itemsCol, statusCol);
        TableColumnLayout.bindEllipsisCellFactory(idCol);
        TableColumnLayout.bindEllipsisCellFactory(createdCol);
        TableColumnLayout.bindEllipsisCellFactory(statusCol);
        bindTableScroll(listTable, tableContainer);

        coOrderCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().orderId()));
        coSiteCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().siteCode()));
        coMerchCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().merchandiseCode()));
        coQtyCol.setCellValueFactory(c -> new SimpleStringProperty(formatOrderedQty(c.getValue())));
        coActualCol.setCellValueFactory(c -> new SimpleStringProperty(formatActualQty(c.getValue().actualQuantity())));
        coDiffCol.setCellValueFactory(c -> new SimpleStringProperty(formatDiff(c.getValue().quantityDiff())));
        coShipCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().deliveryMeansLabel()));
        coStatusCol.setCellValueFactory(c -> new SimpleStringProperty(StatusLabels.orderStatus(c.getValue().orderStatus())));
        TableColumnLayout.bindProportionalColumns(
                childOrdersTable, CHILD_ORDER_COL_RATIOS,
                coOrderCol, coSiteCol, coMerchCol, coQtyCol, coActualCol, coDiffCol, coShipCol, coStatusCol);
        TableColumnLayout.bindEllipsisCellFactory(coOrderCol);
        TableColumnLayout.bindEllipsisCellFactory(coStatusCol);
        bindTableScroll(childOrdersTable, childOrdersContainer);

        listTable.getSelectionModel().selectedItemProperty().addListener((obs, o, row) -> {
            if (row != null) {
                loadDetail(row.requestId());
            }
        });
        onSearch();
    }

    @FXML
    private void onSearch() {
        RequestStatus status = statusFilter.getValue();
        LocalDate from = fromPicker.getValue();
        LocalDate to = toPicker.getValue();
        if (from != null && to != null && from.isAfter(to)) {
            UiTasks.showError(new IllegalArgumentException("Khoảng ngày không hợp lệ: Từ ngày phải nhỏ hơn hoặc bằng Đến ngày."));
            return;
        }
        UiTasks.runWithStatus(
                "Đang tìm…",
                () -> app.uc003().listRequests(status, from, to),
                this::applySearchResults,
                "Danh sách đã cập nhật."
        );
    }

    private void applySearchResults(List<ImportRequestListItemDto> items) {
        listTable.setItems(FXCollections.observableArrayList(items));
    }

    private void loadDetail(String requestId) {
        UiTasks.runWithStatus(
                "Đang tải chi tiết…",
                () -> app.uc003().getRequestDetail(requestId),
                detail -> applyDetailResult(requestId, detail),
                "Chi tiết đã hiển thị."
        );
    }

    private void applyDetailResult(String requestId, Optional<ImportRequestTrackingDetailDto> detail) {
        if (detail.isEmpty()) {
            detailArea.setText("Không tìm thấy yêu cầu \"" + requestId + "\".");
            hideChildOrders();
            return;
        }
        ImportRequestTrackingDetailDto d = detail.get();
        detailArea.setText(formatDetail(d));
        if (d.request().status() == RequestStatus.DA_TACH_DON) {
            childOrdersSection.setVisible(true);
            childOrdersSection.setManaged(true);
            childOrdersTable.setItems(FXCollections.observableArrayList(d.childOrders()));
        } else {
            hideChildOrders();
        }
    }

    private void hideChildOrders() {
        childOrdersSection.setVisible(false);
        childOrdersSection.setManaged(false);
        childOrdersTable.getItems().clear();
    }

    private static String formatDetail(ImportRequestTrackingDetailDto d) {
        StringBuilder sb = new StringBuilder();
        sb.append("Mã: ").append(d.request().requestId()).append('\n');
        sb.append("Trạng thái: ").append(StatusLabels.requestStatus(d.request().status())).append('\n');
        sb.append("Mặt hàng:\n");
        d.request().items().forEach(item -> sb.append("  - ")
                .append(item.merchandiseCode())
                .append(" × ").append(item.quantityOrdered())
                .append(' ').append(item.unit())
                .append(" · nhận ").append(item.desiredDeliveryDate())
                .append('\n'));
        if (d.request().status() == RequestStatus.DA_TACH_DON) {
            sb.append("\nXem bảng bên dưới: phương án tách đơn và trạng thái từng đơn theo Site.");
        } else if (d.request().status() == RequestStatus.TU_CHOI) {
            sb.append("\nYêu cầu đã bị Bộ phận Đặt hàng quốc tế từ chối.");
        } else if (d.request().status() == RequestStatus.LOI) {
            sb.append("\nYêu cầu lỗi — cần Overseas xử lý lại trước khi có đơn con.");
        } else {
            sb.append("\n(Đơn con chỉ hiển thị khi trạng thái là Đã tách đơn.)");
        }
        return sb.toString();
    }

    private static String formatOrderedQty(PurchaseOrderTrackingDto o) {
        return o.quantityOrdered() + " " + o.unit();
    }

    private static String formatActualQty(Integer actual) {
        return actual == null ? "—" : String.valueOf(actual);
    }

    private static String formatDiff(Integer diff) {
        if (diff == null) {
            return "—";
        }
        if (diff == 0) {
            return "0";
        }
        return diff > 0 ? "+" + diff : String.valueOf(diff);
    }

    private static StringConverter<RequestStatus> statusConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(RequestStatus status) {
                return status == null ? "Tất cả trạng thái" : StatusLabels.requestStatus(status);
            }

            @Override
            public RequestStatus fromString(String string) {
                return null;
            }
        };
    }

    private static String formatInstant(Instant instant) {
        return instant == null ? "—" : DT.format(instant);
    }
}
