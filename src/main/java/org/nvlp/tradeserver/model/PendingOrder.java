package org.nvlp.tradeserver.model;

import org.nvlp.tradeserver.model.enumn.Side;

import java.math.BigDecimal;
import java.time.Instant;

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

    public Side getSide() {
        return side;
    }

    public void fill(BigDecimal fillSize) {
        size = size.subtract(fillSize);
    }

    public FilledOrder fullyTransact() {
        return transact(size);
    }
    public FilledOrder transact(BigDecimal size) {
        return new FilledOrder(id, price, size, side.turn(), Instant.now().toEpochMilli());
    }
}
