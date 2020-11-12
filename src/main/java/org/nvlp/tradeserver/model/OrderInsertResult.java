package org.nvlp.tradeserver.model;

import java.util.List;

public class OrderInsertResult {

    private List<FilledOrder> filledOrderList;

    private PendingOrder pendingOrder;

    public void addPending(PendingOrder pendingOrder) {
        this.pendingOrder = pendingOrder;
    }

    public List<FilledOrder> getFilledOrderList() {
        return filledOrderList;
    }

    public PendingOrder getPendingOrder() {
        return pendingOrder;
    }
}
