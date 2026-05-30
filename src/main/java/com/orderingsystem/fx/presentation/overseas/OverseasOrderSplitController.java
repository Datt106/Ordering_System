package com.orderingsystem.fx.presentation.overseas;

import com.orderingsystem.core.domain.DeliveryMeans;
import com.orderingsystem.uc002.boundary.dto.ImportRequestDto;
import com.orderingsystem.uc007.boundary.dto.ManualSplitLineInput;
import com.orderingsystem.uc007.boundary.dto.ManualSplitValidationResultDto;
import com.orderingsystem.uc007.boundary.dto.OrderSplitLineDto;
import com.orderingsystem.uc007.boundary.dto.OrderSplitResultDto;
import com.orderingsystem.fx.presentation.BaseViewController;
import com.orderingsystem.fx.presentation.UiTasks;
import com.orderingsystem.fx.presentation.ux.TableColumnLayout;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.IntegerStringConverter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OverseasOrderSplitController extends BaseViewController {

    private static final double[] REQUEST_COL_RATIOS = {0.45, 0.55};
    private static final double[] PLAN_COL_RATIOS = {0.18, 0.18, 0.14, 0.35, 0.15};

    @FXML
    private TableView<ImportRequestDto> processingTable;
    @FXML
    private TableColumn<ImportRequestDto, String> idCol;
    @FXML
    private TableColumn<ImportRequestDto, String> byCol;
    @FXML
    private TableView<EditableSplitLineRow> planTable;
    @FXML
    private TableColumn<EditableSplitLineRow, String> siteCol;
    @FXML
    private TableColumn<EditableSplitLineRow, String> merchCol;
    @FXML
    private TableColumn<EditableSplitLineRow, Integer> qtyCol;
    @FXML
    private TableColumn<EditableSplitLineRow, DeliveryMeans> meansCol;
    @FXML
    private TextField addSiteField;
    @FXML
    private TextField addMerchField;
    @FXML
    private Spinner<Integer> addQtySpinner;
    @FXML
    private ChoiceBox<DeliveryMeans> addMeansChoice;
    @FXML
    private TextArea validationArea;
    @FXML
    private Button confirmButton;

    private String activeRequestId;
    private LocalDate activeStartDate = LocalDate.now();
    private final List<EditableSplitLineRow> planLines = new ArrayList<>();

    @Override
    protected void onInit() {
        idCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().requestId()));
        byCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().processedBy() != null ? c.getValue().processedBy() : "—"));

        siteCol.setCellValueFactory(c -> c.getValue().siteCodeProperty());
        merchCol.setCellValueFactory(c -> c.getValue().merchandiseCodeProperty());
        qtyCol.setCellValueFactory(c -> c.getValue().quantityProperty().asObject());
        meansCol.setCellValueFactory(c -> c.getValue().deliveryMeansProperty());

        planTable.setEditable(true);
        siteCol.setCellFactory(TextFieldTableCell.forTableColumn());
        merchCol.setCellFactory(TextFieldTableCell.forTableColumn());
        qtyCol.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        meansCol.setCellFactory(col -> new ChoiceBoxTableCell<>(DeliveryMeans.values()));

        siteCol.setOnEditCommit(e -> e.getRowValue().siteCodeProperty().set(e.getNewValue()));
        merchCol.setOnEditCommit(e -> e.getRowValue().merchandiseCodeProperty().set(e.getNewValue()));
        qtyCol.setOnEditCommit(e -> e.getRowValue().quantityProperty().set(e.getNewValue()));
        meansCol.setOnEditCommit(e -> e.getRowValue().deliveryMeansProperty().set(e.getNewValue()));

        TableColumnLayout.bindProportionalColumns(processingTable, REQUEST_COL_RATIOS, idCol, byCol);
        TableColumnLayout.bindProportionalColumns(planTable, PLAN_COL_RATIOS, siteCol, merchCol, qtyCol, meansCol);
        TableColumnLayout.bindEllipsisCellFactory(siteCol);
        TableColumnLayout.bindEllipsisCellFactory(merchCol);

        addQtySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999_999, 1));
        addMeansChoice.getItems().setAll(DeliveryMeans.values());
        addMeansChoice.setValue(DeliveryMeans.SHIP_DELIVERY);

        bindEmptyTable(processingTable, "Không có yêu cầu Đang xử lý.");
        bindEmptyTable(planTable, "Chọn yêu cầu rồi bấm Sinh phương án tự động.");
        confirmButton.setDisable(true);
        refreshRequests();
    }

    @FXML
    private void onRefresh() {
        refreshRequests();
    }

    @FXML
    private void onGeneratePlan() {
        ImportRequestDto selected = requireRequestSelection();
        if (selected == null) {
            return;
        }
        activeRequestId = selected.requestId();
        activeStartDate = LocalDate.now();
        UiTasks.runWithStatus(
                "Đang sinh phương án…",
                () -> app.uc007().previewSplit(activeRequestId, activeStartDate),
                this::applyPreview,
                "Phương án đã tải — có thể chỉnh sửa trước khi kiểm tra."
        );
    }

    @FXML
    private void onValidatePlan() {
        if (activeRequestId == null) {
            setScreenStatus("Chọn yêu cầu và sinh phương án trước.");
            return;
        }
        List<ManualSplitLineInput> inputs = toManualInputs();
        UiTasks.runWithStatus(
                "Đang kiểm tra phương án…",
                () -> app.uc007().validateManualSplit(activeRequestId, activeStartDate, inputs),
                this::applyValidation,
                "Kiểm tra hoàn tất."
        );
    }

    @FXML
    private void onConfirmPlan() {
        if (activeRequestId == null) {
            setScreenStatus("Chưa có yêu cầu đang xử lý phương án.");
            return;
        }
        if (!UiTasks.confirm(
                "Xác nhận tách đơn",
                "Lưu phương án cho " + activeRequestId + "?",
                "Hệ thống sẽ tạo các đơn con ở trạng thái Chờ gửi."
        )) {
            setScreenStatus("Đã hủy xác nhận.");
            return;
        }
        List<ManualSplitLineInput> inputs = toManualInputs();
        UiTasks.runWithStatus(
                "Đang lưu phương án…",
                () -> app.uc007().confirmManualSplit(activeRequestId, activeStartDate, inputs),
                result -> {
                    confirmButton.setDisable(true);
                    validationArea.setText("Đã tạo " + result.allLines().size() + " đơn con — Chờ gửi (UC008).");
                    setScreenStatus("Tách đơn thành công cho " + activeRequestId + ".");
                    UiTasks.showInfo("Thành công", "Đã lưu " + result.allLines().size() + " đơn con.");
                },
                "Hoàn tất."
        );
    }

    @FXML
    private void onAddLine() {
        String site = addSiteField.getText() != null ? addSiteField.getText().trim() : "";
        String merch = addMerchField.getText() != null ? addMerchField.getText().trim() : "";
        if (site.isBlank() || merch.isBlank()) {
            setScreenStatus("Nhập mã Site và mã hàng trước khi thêm dòng.");
            return;
        }
        EditableSplitLineRow row = new EditableSplitLineRow();
        row.siteCodeProperty().set(site);
        row.merchandiseCodeProperty().set(merch);
        row.quantityProperty().set(addQtySpinner.getValue());
        row.deliveryMeansProperty().set(addMeansChoice.getValue());
        planLines.add(row);
        refreshPlanTable();
        confirmButton.setDisable(true);
        setScreenStatus("Đã thêm dòng — bấm Kiểm tra phương án trước khi xác nhận.");
    }

    @FXML
    private void onRemoveLine() {
        EditableSplitLineRow selected = planTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setScreenStatus("Chọn dòng cần xóa trong bảng phương án.");
            return;
        }
        planLines.remove(selected);
        refreshPlanTable();
        confirmButton.setDisable(true);
        setScreenStatus("Đã xóa một dòng phương án.");
    }

    private void refreshRequests() {
        UiTasks.runWithStatus(
                "Đang tải…",
                () -> app.uc005().listProcessingRequests(),
                items -> {
                    processingTable.setItems(FXCollections.observableArrayList(items));
                    setScreenStatus(items.isEmpty()
                            ? "Chưa có yêu cầu đang xử lý."
                            : items.size() + " yêu cầu — chọn rồi Sinh phương án.");
                },
                "Danh sách sẵn sàng."
        );
    }

    private ImportRequestDto requireRequestSelection() {
        ImportRequestDto selected = processingTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setScreenStatus("Chọn một yêu cầu Đang xử lý trong bảng.");
        }
        return selected;
    }

    private void applyPreview(OrderSplitResultDto preview) {
        planLines.clear();
        for (OrderSplitLineDto line : preview.allLines()) {
            planLines.add(EditableSplitLineRow.from(line));
        }
        refreshPlanTable();
        confirmButton.setDisable(true);

        StringBuilder sb = new StringBuilder();
        preview.plans().forEach(plan -> {
            if (plan.success()) {
                sb.append("✓ ").append(plan.merchandiseCode())
                        .append(" — ").append(plan.quantityNeeded()).append(" đơn vị\n");
            } else {
                sb.append("✗ ").append(plan.merchandiseCode())
                        .append(": ").append(plan.errorMessage()).append('\n');
            }
        });
        if (!preview.readyToConfirm()) {
            sb.append("\nChưa đủ phản hồi tồn kho — chỉ xem trước, chưa thể xác nhận.");
        }
        validationArea.setText(sb.toString());
        setScreenStatus(preview.allLines().isEmpty()
                ? "Chưa có dòng phân bổ hợp lệ — kiểm tra tồn kho hoặc thêm tay."
                : preview.allLines().size() + " dòng phương án — chỉnh sửa rồi Kiểm tra.");
    }

    private void applyValidation(ManualSplitValidationResultDto result) {
        if (result.valid()) {
            validationArea.setText("Phương án hợp lệ — có thể xác nhận.\n"
                    + result.preview().allLines().size() + " dòng phân bổ.");
            confirmButton.setDisable(false);
            setScreenStatus("Phương án hợp lệ — bấm Xác nhận để tạo đơn con.");
        } else {
            validationArea.setText(String.join("\n", result.errors()));
            confirmButton.setDisable(true);
            setScreenStatus("Phương án chưa hợp lệ — sửa bảng và kiểm tra lại.");
        }
    }

    private List<ManualSplitLineInput> toManualInputs() {
        return planLines.stream()
                .map(row -> new ManualSplitLineInput(
                        row.getSiteCode(),
                        row.getMerchandiseCode(),
                        row.getQuantity(),
                        row.getDeliveryMeans()))
                .toList();
    }

    private void refreshPlanTable() {
        planTable.setItems(FXCollections.observableArrayList(planLines));
    }
}
