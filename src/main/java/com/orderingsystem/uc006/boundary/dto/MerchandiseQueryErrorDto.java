package com.orderingsystem.uc006.boundary.dto;

/** Mặt hàng trong yêu cầu không có Site kinh doanh (UC006 — 2a). */
public record MerchandiseQueryErrorDto(String merchandiseCode, String message) {
}
