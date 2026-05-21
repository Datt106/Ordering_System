package com.orderingsystem.domain.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Danh mục mặt hàng chuẩn (SRS A03) — dùng validate UC002 / UC009.
 */
@Entity
@Table(name = "standard_merchandise")
public class StandardMerchandise {

    @Id
    @Column(name = "merchandise_code", length = 64)
    private String merchandiseCode;

    protected StandardMerchandise() {
    }

    public StandardMerchandise(String merchandiseCode) {
        this.merchandiseCode = merchandiseCode;
    }

    public String getMerchandiseCode() {
        return merchandiseCode;
    }
}
