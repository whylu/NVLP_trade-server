package org.nvlp.tradeserver.model;

import org.nvlp.tradeserver.model.enumn.Side;

import java.math.BigDecimal;
import java.time.Instant;

public class PendingOrder {
    private Side side;
    private long id;
    private double price;
    private BigDecimal origSize;
    private BigDecimal size; // remain size

    public PendingOrder(long orderId, double price, BigDecimal size, Side side) {
        this(orderId, price, size, size, side);
    }
    protected PendingOrder(long orderId, double price, BigDecimal origSize, BigDecimal size, Side side) {
        id = orderId;
        this.origSize = origSize;
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

    public BigDecimal getOrigSize() {
        return origSize;
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
        this.size = this.size.subtract(size);
        return new FilledOrder(id, price, origSize, size, side.turn(), Instant.now().toEpochMilli());
    }
}
