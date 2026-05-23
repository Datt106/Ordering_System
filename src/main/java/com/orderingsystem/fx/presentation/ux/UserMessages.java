package com.orderingsystem.fx.presentation.ux;

import java.util.regex.Pattern;

/**
 * Heuristic #9 — Thông báo lỗi dễ hiểu + gợi ý khắc phục (ẩn chi tiết kỹ thuật với người dùng).
 */
public final class UserMessages {

    private static final Pattern NOT_FOUND_CODE = Pattern.compile("không tồn tại", Pattern.CASE_INSENSITIVE);
    private static final Pattern ALREADY_EXISTS = Pattern.compile("đã tồn tại|đã có", Pattern.CASE_INSENSITIVE);
    private static final Pattern PERMISSION = Pattern.compile("không có quyền|SecurityException", Pattern.CASE_INSENSITIVE);
    private static final Pattern BLANK_FIELD = Pattern.compile("không được để trống", Pattern.CASE_INSENSITIVE);
    private static final Pattern DATE = Pattern.compile("ngày nhận|ngày hiện tại", Pattern.CASE_INSENSITIVE);
    private static final Pattern LOGIN = Pattern.compile("đăng nhập|mật khẩu", Pattern.CASE_INSENSITIVE);

    private UserMessages() {
    }

    public record FriendlyError(String summary, String recoveryHint) {
    }

    public static FriendlyError from(Throwable error) {
        String raw = error.getMessage() != null ? error.getMessage() : error.getClass().getSimpleName();
        String summary = raw;
        String hint = "Thử lại hoặc liên hệ quản trị nếu lỗi lặp lại.";

        if (error instanceof IllegalStateException && raw.contains("trạng thái")) {
            hint = "Kiểm tra trạng thái yêu cầu trên màn theo dõi / tiếp nhận trước khi thực hiện bước tiếp theo.";
        } else if (NOT_FOUND_CODE.matcher(raw).find()) {
            hint = "Kiểm tra mã đã nhập hoặc mở danh mục / danh sách Site để chọn đúng mã.";
        } else if (ALREADY_EXISTS.matcher(raw).find()) {
            hint = "Dùng chức năng cập nhật thay vì thêm mới, hoặc chọn mã khác.";
        } else if (PERMISSION.matcher(raw).find()) {
            hint = "Đăng xuất và đăng nhập bằng tài khoản đúng vai trò cho thao tác này.";
        } else if (BLANK_FIELD.matcher(raw).find()) {
            hint = "Điền đầy đủ các trường bắt buộc (có dấu *) trước khi gửi.";
        } else if (DATE.matcher(raw).find()) {
            hint = "Chọn ngày nhận hàng sau ngày hôm nay.";
        } else if (LOGIN.matcher(raw).find()) {
            hint = "Xem gợi ý tài khoản demo bên dưới form đăng nhập hoặc hỏi quản trị.";
        } else if (raw.contains("Site") && raw.contains("vận chuyển")) {
            hint = "Site cần khai báo số ngày tàu/bay trước khi hệ thống gửi truy vấn tồn kho.";
        }

        return new FriendlyError(summary, hint);
    }
}
