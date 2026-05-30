package com.orderingsystem.fx.presentation;

import com.orderingsystem.core.domain.OrderStatus;
import com.orderingsystem.core.domain.RequestStatus;
import com.orderingsystem.core.domain.ShippingStatus;

public final class StatusLabels {

    private StatusLabels() {
    }

    public static String requestStatus(RequestStatus status) {
        if (status == null) {
            return "—";
        }
        return switch (status) {
            case CHO_XU_LY -> "Chờ xử lý";
            case DANG_XU_LY -> "Đang xử lý";
            case DA_TACH_DON -> "Đã tách đơn";
            case LOI -> "Lỗi";
        };
    }

    public static String orderStatus(OrderStatus status) {
        if (status == null) {
            return "—";
        }
        return switch (status) {
            case CHO_GUI -> "Chờ gửi";
            case DA_GUI -> "Đã gửi";
            case DA_XAC_NHAN -> "Đã xác nhận";
            case TU_CHOI -> "Từ chối";
            case DA_NHAP_KHO -> "Đã nhập kho";
            case SAI_LECH -> "Sai lệch";
        };
    }

    public static String shippingStatus(ShippingStatus status) {
        if (status == null) {
            return "—";
        }
        return switch (status) {
            case CHUA_KHAI_BAO -> "Chưa khai báo";
            case DA_KHAI_BAO -> "Đã khai báo";
        };
    }
}
