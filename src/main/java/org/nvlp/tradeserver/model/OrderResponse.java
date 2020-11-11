package org.nvlp.tradeserver.model;

import org.nvlp.tradeserver.model.enumn.OrderStatus;
import org.nvlp.tradeserver.model.enumn.OrderType;
import org.nvlp.tradeserver.model.enumn.Side;

public class OrderResponse {
    private String symbol;
    private String orderId;
    private String tradeId;
    private OrderStatus status;
    private OrderType type;
    private Side side;
    private double price;    // original price
    private double origSize; // original size
    private long transactTime;


}
