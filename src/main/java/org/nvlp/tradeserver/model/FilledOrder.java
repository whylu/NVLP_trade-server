package org.nvlp.tradeserver.model;

import org.nvlp.tradeserver.model.enumn.Side;

import java.math.BigDecimal;

public class FilledOrder extends PendingOrder {

    protected FilledOrder(long orderId, double price, BigDecimal origSize, BigDecimal size, Side side, long timestamp) {
        super(orderId, price, origSize, size, side);
        this.timestamp = timestamp;
    }
    private long timestamp;

    public long getTimestamp() {
        return timestamp;
    }
}
