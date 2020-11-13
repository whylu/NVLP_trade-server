package org.nvlp.tradeserver.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.nvlp.tradeserver.model.enumn.Side;

import java.math.BigDecimal;
import java.time.Instant;

public class PendingOrder {
    private Side side;
    private long id;
    private double price;
    private BigDecimal origSize;
    protected BigDecimal size; // remain size

    // derivative values
    protected boolean filled = false;

    public PendingOrder(long orderId, double price, BigDecimal size, Side side) {
        id = orderId;
        this.origSize = size;
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
        this.filled = this.size.compareTo(BigDecimal.ZERO)==0;
        FilledOrder filledOrder = new FilledOrder(id, price, origSize, side.turn(), Instant.now().toEpochMilli());
        filledOrder.size = size;
        filledOrder.filled = this.filled;
        return filledOrder;
    }

    @JsonIgnore
    public boolean isFilled() {
        return filled;
    }
}
