package com.orderingsystem.fx.presentation.sales;

import com.orderingsystem.domain.request.RequestStatus;
import com.orderingsystem.uc003.dto.ImportRequestListItemDto;
import com.orderingsystem.uc003.dto.ImportRequestTrackingDetailDto;
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
import javafx.scene.control.TextField;
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

    @FXML
    private ComboBox<RequestStatus> statusFilter;
    @FXML
    private DatePicker fromPicker;
    @FXML
    private DatePicker toPicker;
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
    private TextField detailIdField;
    @FXML
    private TextArea detailArea;

    @Override
    protected void onInit() {
        statusFilter.setItems(FXCollections.observableArrayList(
                null,
                RequestStatus.CHO_XU_LY,
                RequestStatus.DANG_XU_LY,
                RequestStatus.DA_TACH_DON,
                RequestStatus.LOI
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

        listTable.getSelectionModel().selectedItemProperty().addListener((obs, o, row) -> {
            if (row != null) {
                detailIdField.setText(row.requestId());
                onShowDetail();
            }
        });
        bindEmptyTable(listTable, "Chưa có yêu cầu nào khớp bộ lọc. Thử bỏ lọc trạng thái hoặc mở rộng khoảng ngày.");
        onSearch();
    }

    @FXML
    private void onSearch() {
        RequestStatus status = statusFilter.getValue();
        LocalDate from = fromPicker.getValue();
        LocalDate to = toPicker.getValue();
        UiTasks.runWithStatus(
                "Đang tìm…",
                () -> app.tracking().listRequests(status, from, to),
                this::applySearchResults,
                "Danh sách đã cập nhật."
        );
    }

    private void applySearchResults(List<ImportRequestListItemDto> items) {
        listTable.setItems(FXCollections.observableArrayList(items));
        setScreenStatus(items.isEmpty()
                ? "Không có kết quả."
                : "Tìm thấy " + items.size() + " yêu cầu — chọn dòng để xem chi tiết.");
    }

    @FXML
    private void onShowDetail() {
        String id = detailIdField.getText();
        if (id == null || id.isBlank()) {
            detailArea.setText("Chọn một dòng trong bảng hoặc nhập mã yêu cầu.");
            setScreenStatus("Chưa chọn yêu cầu.");
            return;
        }
        String requestId = id.trim();
        UiTasks.runWithStatus(
                "Đang tải chi tiết…",
                () -> app.tracking().getRequestDetail(requestId),
                detail -> applyDetailResult(requestId, detail),
                "Chi tiết đã hiển thị."
        );
    }

    private void applyDetailResult(String requestId, Optional<ImportRequestTrackingDetailDto> detail) {
        if (detail.isEmpty()) {
            detailArea.setText("Không tìm thấy yêu cầu \"" + requestId + "\".\nKiểm tra mã hoặc bấm Tìm để làm mới danh sách.");
            setScreenStatus("Không tìm thấy yêu cầu.");
            return;
        }
        detailArea.setText(formatDetail(detail.get()));
        setScreenStatus("Chi tiết: " + requestId);
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
        if (!d.childOrders().isEmpty()) {
            sb.append("Đơn con:\n");
            d.childOrders().forEach(o -> sb.append("  - ").append(o.orderId())
                    .append(" @ ").append(o.siteCode()).append('\n'));
        } else {
            sb.append("\n(Chưa có đơn con — yêu cầu có thể vẫn đang xử lý.)");
        }
        return sb.toString();
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
