package org.nvlp.tradeserver.model;

import java.math.BigDecimal;

public class FilledOrder extends PendingOrder {

    private long timestamp;

    public FilledOrder(long orderId, double price, BigDecimal size, long timestamp) {
        super(orderId, price, size);
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
