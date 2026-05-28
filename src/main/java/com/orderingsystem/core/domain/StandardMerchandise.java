package com.orderingsystem.core.domain;

/**
 * Danh mục mặt hàng chuẩn do Sales duy trì — mã + tên/mô tả để Site/Overseas tra cứu khi chọn hàng.
 */
public class StandardMerchandise {

    private String merchandiseCode;

    /** Bắt buộc khi Sales đăng ký; nullable ở DB để migrate SQLite cũ. */
    private String merchandiseName;

    private String description;

    protected StandardMerchandise() {
    }

    public StandardMerchandise(String merchandiseCode, String merchandiseName, String description) {
        this.merchandiseCode = merchandiseCode;
        this.merchandiseName = merchandiseName;
        this.description = description;
    }

    public String getMerchandiseCode() {
        return merchandiseCode;
    }

    public String getMerchandiseName() {
        return merchandiseName;
    }

    public void setMerchandiseName(String merchandiseName) {
        this.merchandiseName = merchandiseName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
