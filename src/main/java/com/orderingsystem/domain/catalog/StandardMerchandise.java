package com.orderingsystem.domain.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Danh mục mặt hàng chuẩn do Sales duy trì — mã + tên/mô tả để Site/Overseas tra cứu khi chọn hàng.
 */
@Entity
@Table(name = "standard_merchandise")
public class StandardMerchandise {

    @Id
    @Column(name = "merchandise_code", length = 64)
    private String merchandiseCode;

    /** Bắt buộc khi Sales đăng ký; nullable ở DB để migrate SQLite cũ. */
    @Column(name = "merchandise_name", length = 255)
    private String merchandiseName;

    @Column(length = 2000)
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
