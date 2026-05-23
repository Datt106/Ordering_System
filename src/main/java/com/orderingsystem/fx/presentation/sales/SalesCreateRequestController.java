package com.orderingsystem.fx.presentation.sales;

import com.orderingsystem.uc002.dto.CreateImportRequestLineInput;
import com.orderingsystem.uc002.dto.ImportRequestDto;
import com.orderingsystem.fx.presentation.BaseViewController;
import com.orderingsystem.fx.presentation.UiTasks;
import com.orderingsystem.fx.presentation.ux.FormValidation;
import com.orderingsystem.fx.presentation.ux.TableColumnLayout;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SalesCreateRequestController extends BaseViewController {

    private static final double[] LINE_COL_RATIOS = {0.25, 0.15, 0.15, 0.45};
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    private TextField codeField;
    @FXML
    private Spinner<Integer> qtySpinner;
    @FXML
    private TextField unitField;
    @FXML
    private DatePicker deliveryPicker;
    @FXML
    private TableView<LineRow> linesTable;
    @FXML
    private TableColumn<LineRow, String> codeCol;
    @FXML
    private TableColumn<LineRow, String> qtyCol;
    @FXML
    private TableColumn<LineRow, String> unitCol;
    @FXML
    private TableColumn<LineRow, String> dateCol;
    @FXML
    private Label resultLabel;
    @FXML
    private Button submitButton;
    @FXML
    private Button addLineButton;

    private final List<LineRow> lines = new ArrayList<>();

    @Override
    protected void onInit() {
        qtySpinner.setValueFactory(new javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999_999, 1));
        codeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().code()));
        qtyCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().qty())));
        unitCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().unit()));
        dateCol.setCellValueFactory(c -> new SimpleStringProperty(formatDate(c.getValue().deliveryDate())));

        TableColumnLayout.bindProportionalColumns(linesTable, LINE_COL_RATIOS, codeCol, qtyCol, unitCol, dateCol);
        TableColumnLayout.bindEllipsisCellFactory(codeCol);
        TableColumnLayout.bindEllipsisCellFactory(unitCol);
        TableColumnLayout.bindEllipsisCellFactory(dateCol);

        deliveryPicker.setValue(LocalDate.now().plusDays(7));
        unitField.setText("pcs");
        bindEmptyTable(linesTable, "Chưa có dòng nào — nhập mã hàng, số lượng, đơn vị và ngày nhận rồi bấm Thêm dòng.");
        FormValidation.bindDisabledUntilFilled(addLineButton, codeField, unitField);
        updateSubmitState();
        setScreenStatus("Ngày nhận mặc định: 7 ngày sau hôm nay — có thể đổi trước khi thêm dòng.");
        refreshTable();
    }

    @FXML
    private void onAddLine() {
        LocalDate date = deliveryPicker.getValue();
        try {
            FormValidation.requireNonBlank(codeField, "Nhập mã hàng.", () -> setScreenStatus("Thiếu mã hàng."));
            FormValidation.requireNonBlank(unitField, "Nhập đơn vị.", () -> setScreenStatus("Thiếu đơn vị."));
            if (date == null) {
                setScreenStatus("Chọn ngày nhận mong muốn.");
                throw new IllegalArgumentException("Chọn ngày nhận mong muốn.");
            }
            if (!date.isAfter(LocalDate.now())) {
                setScreenStatus("Ngày nhận phải sau hôm nay.");
                throw new IllegalArgumentException("Ngày nhận mong muốn phải sau ngày hiện tại.");
            }
        } catch (IllegalArgumentException ex) {
            UiTasks.showError(ex);
            return;
        }
        lines.add(new LineRow(
                codeField.getText().trim(),
                qtySpinner.getValue(),
                unitField.getText().trim(),
                date
        ));
        refreshTable();
        setScreenStatus("Đã thêm dòng. Tổng: " + lines.size() + " — kiểm tra bảng trước khi gửi.");
        resultLabel.setText("");
    }

    @FXML
    private void onRemoveLine() {
        LineRow selected = linesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setScreenStatus("Chọn dòng cần xóa trong bảng.");
            return;
        }
        lines.remove(selected);
        refreshTable();
        setScreenStatus("Đã xóa một dòng. Còn " + lines.size() + " dòng.");
    }

    @FXML
    private void onClearAll() {
        if (lines.isEmpty()) {
            return;
        }
        if (!UiTasks.confirm("Xóa tất cả dòng", "Xóa toàn bộ dòng đã nhập?", "Bạn sẽ phải nhập lại từ đầu.")) {
            return;
        }
        lines.clear();
        refreshTable();
        setScreenStatus("Đã xóa hết dòng.");
        resultLabel.setText("");
    }

    @FXML
    private void onSubmit() {
        if (lines.isEmpty()) {
            setScreenStatus("Thêm ít nhất một dòng trước khi gửi.");
            return;
        }
        if (!UiTasks.confirm(
                "Gửi yêu cầu",
                "Gửi yêu cầu nhập hàng với " + lines.size() + " dòng?",
                "Yêu cầu sẽ chuyển sang trạng thái Chờ xử lý cho bộ phận Đặt hàng quốc tế."
        )) {
            setScreenStatus("Đã hủy gửi.");
            return;
        }
        List<CreateImportRequestLineInput> inputs = lines.stream()
                .map(r -> new CreateImportRequestLineInput(r.code(), r.qty(), r.unit(), r.deliveryDate()))
                .toList();
        UiTasks.runWithStatus(
                "Đang gửi yêu cầu…",
                () -> app.importRequests().createImportRequest(inputs),
                created -> {
                    lines.clear();
                    refreshTable();
                    resultLabel.setText("Mã yêu cầu: " + created.requestId());
                    setScreenStatus("Đã tạo yêu cầu " + created.requestId());
                    UiTasks.showInfo(
                            "Yêu cầu đã gửi",
                            "Mã: " + created.requestId() + "\nTheo dõi tiến độ tại menu Theo dõi yêu cầu."
                    );
                },
                "Sẵn sàng tạo yêu cầu mới."
        );
    }

    private void refreshTable() {
        linesTable.setItems(FXCollections.observableArrayList(lines));
        updateSubmitState();
    }

    private void updateSubmitState() {
        submitButton.setDisable(lines.isEmpty());
    }

    private static String formatDate(LocalDate date) {
        return date == null ? "—" : DATE_FMT.format(date);
    }

    public record LineRow(String code, int qty, String unit, LocalDate deliveryDate) {
    }
}
