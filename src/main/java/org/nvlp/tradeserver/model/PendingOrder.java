package org.nvlp.tradeserver.model;

import java.math.BigDecimal;

public class PendingOrder {
    private long id;
    private double price;
    private BigDecimal size; // remain size

    public PendingOrder(long orderId, double price, BigDecimal size) {
        id = orderId;
        this.price = price;
        this.size = size;
    }

    public long getId() {
        return id;
    }

    public double getPrice() {
        return price;
    }

    public BigDecimal getSize() {
        return size;
    }
}
