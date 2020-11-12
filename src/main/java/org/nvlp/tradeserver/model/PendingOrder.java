package org.nvlp.tradeserver.model;

import org.nvlp.tradeserver.model.enumn.Side;

import java.math.BigDecimal;

public class PendingOrder {
    private Side side;
    private long id;
    private double price;
    private BigDecimal size; // remain size

    public PendingOrder(long orderId, double price, BigDecimal size, Side side) {
        id = orderId;
        this.price = price;
        this.size = size;
        this.side = side;
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
