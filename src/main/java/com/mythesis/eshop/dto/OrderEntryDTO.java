package com.mythesis.eshop.dto;

public class OrderEntryDTO {

    private Long userId;
    private Double total;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }


}
