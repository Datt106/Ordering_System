package com.orderingsystem.fx.navigation;

import com.orderingsystem.core.domain.UserRole;

import java.util.List;

/**
 * Information Expert: mapping vai trò → màn hình + trợ giúp ngắn (Nielsen #10).
 */
public final class RoleMenuFactory {

    private RoleMenuFactory() {
    }

    public static List<ScreenDefinition> screensFor(UserRole role) {
        return switch (role) {
            case SALES -> List.of(
                    new ScreenDefinition(
                            "Danh mục hàng chuẩn",
                            "/fxml/sales/CatalogView.fxml",
                            "Duy trì mã hàng chuẩn trước khi tạo yêu cầu nhập. Mã phải tồn tại khi Sales nhập dòng đặt hàng."),
                    new ScreenDefinition(
                            "Tạo yêu cầu nhập",
                            "/fxml/sales/CreateRequestView.fxml",
                            "Thêm từng dòng mặt hàng, kiểm tra ngày nhận > hôm nay, rồi gửi. Hệ thống báo mã yêu cầu sau khi lưu."),
                    new ScreenDefinition(
                            "Theo dõi yêu cầu",
                            "/fxml/sales/TrackingView.fxml",
                            "Lọc theo trạng thái hoặc ngày tạo. Chọn dòng và bấm Chi tiết để xem tiến độ và đơn con (nếu có).")
            );
            case OVERSEAS -> List.of(
                    new ScreenDefinition(
                            "Quản lý Site",
                            "/fxml/overseas/SitesView.fxml",
                            "Đăng ký đối tác nhập khẩu. Site tự khai báo vận chuyển và mặt hàng kinh doanh trên tài khoản Site."),
                    new ScreenDefinition(
                            "Tiếp nhận yêu cầu",
                            "/fxml/overseas/PendingRequestsView.fxml",
                            "Chỉ yêu cầu Chờ xử lý. Xem chi tiết bên phải, bấm Tiếp nhận để chuyển sang Đang xử lý."),
                    new ScreenDefinition(
                            "Truy vấn tồn kho",
                            "/fxml/overseas/InventoryQueryView.fxml",
                            "Chọn yêu cầu Đang xử lý → Gửi truy vấn. Site phản hồi trên màn Xác nhận tồn kho. Timeout → 0 chỉ khi Site không trả lời."),
                    new ScreenDefinition(
                            "Tách đơn hàng",
                            "/fxml/overseas/OrderSplitView.fxml",
                            "Chọn yêu cầu Đang xử lý → Sinh phương án tự động → chỉnh tay từng dòng → Kiểm tra → Xác nhận tạo đơn con Chờ gửi."),
                    new ScreenDefinition(
                            "Gửi đơn hàng",
                            "/fxml/overseas/OrderDispatchView.fxml",
                            "Chọn yêu cầu đã tách đơn → Xem trước danh sách đơn con → Gửi đơn tới Site.")
            );
            case SITE -> List.of(
                    new ScreenDefinition(
                            "Vận chuyển",
                            "/fxml/site/ShippingView.fxml",
                            "Khai báo số ngày tàu và bay — bắt buộc trước khi hệ thống gửi truy vấn tồn kho tới Site của bạn."),
                    new ScreenDefinition(
                            "Mặt hàng kinh doanh",
                            "/fxml/site/MerchandiseView.fxml",
                            "Chọn mã từ danh mục chuẩn công ty. Chỉ mặt hàng đã khai báo mới nhận truy vấn tồn kho."),
                    new ScreenDefinition(
                            "Xác nhận tồn kho",
                            "/fxml/site/InventoryResponseView.fxml",
                            "Chọn dòng chờ phản hồi, nhập số lượng tồn (≥ 0), bấm Gửi phản hồi."),
                    new ScreenDefinition(
                            "Tiếp nhận đơn hàng",
                            "/fxml/site/SiteOrderConfirmView.fxml",
                            "Đơn Đã gửi từ Overseas: Xác nhận hoặc Từ chối từng dòng.")
            );
            case WAREHOUSE -> List.of(
                    new ScreenDefinition(
                            "Danh sách đơn hàng",
                            "/fxml/warehouse/WarehouseOrdersView.fxml",
                            "Lọc theo trạng thái, Site hoặc mã hàng. Theo dõi đơn đã gửi và đã xác nhận từ Site."),
                    new ScreenDefinition(
                            "Đối chiếu nhập kho",
                            "/fxml/warehouse/WarehouseReconcileView.fxml",
                            "Chọn đơn, nhập số lượng thực nhận và ghi nhận nhập kho hoặc sai lệch.")
            );
        };
    }

    public static String roleTitle(UserRole role) {
        return switch (role) {
            case SALES -> "Bộ phận Bán hàng";
            case OVERSEAS -> "Đặt hàng quốc tế";
            case SITE -> "Site nhập khẩu";
            case WAREHOUSE -> "Quản lý kho";
        };
    }

    public static String loginHelpText() {
        return """
                Nội bộ (tài khoản cố định): sales / sales123 · overseas / overseas123 · warehouse / wh123
                Site demo: site01 / site123 (Site S01). Site mới: Overseas thêm mã Site → Đăng ký tài khoản Site.
                """;
    }
}
