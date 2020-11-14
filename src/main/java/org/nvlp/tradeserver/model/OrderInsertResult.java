package org.nvlp.tradeserver.model;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class OrderInsertResult {
    private long orderId;
    private BigDecimal filledSize = BigDecimal.ZERO;

    private List<FilledOrder> filledOrderList = Collections.emptyList();

    private PendingOrder pendingOrder;

    public OrderInsertResult(long orderId) {
        this.orderId = orderId;
    }

    public long getOrderId() {
        return orderId;
    }

    public void addPending(PendingOrder pendingOrder) {
        this.pendingOrder = pendingOrder;
    }

    public List<FilledOrder> getFilledOrderList() {
        return filledOrderList;
    }

    public void setFilledOrderList(List<FilledOrder> filledOrderList) {
        this.filledOrderList = filledOrderList;
    }

    public PendingOrder getPendingOrder() {
        return pendingOrder;
    }

    public BigDecimal getFilledSize() {
        return filledSize;
    }

    public void setFilledSize(BigDecimal filledSize) {
        this.filledSize = filledSize;
    }
}
