package org.nvlp.tradeserver.model;

import org.nvlp.tradeserver.dto.PlaceOrderRequest;
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
    private double size; // original size
    private long transactTime;

    public static OrderResponse of(PlaceOrderRequest request) {
        OrderResponse r = new OrderResponse();
        r.symbol = request.getSymbol();
        r.size = request.getSize();
        r.price = request.getPrice();
        r.type = request.getType();
        r.side = request.getSide();
        return r;
    }


    public String getSymbol() {
        return symbol;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getTradeId() {
        return tradeId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public OrderType getType() {
        return type;
    }

    public Side getSide() {
        return side;
    }

    public double getPrice() {
        return price;
    }

    public double getSize() {
        return size;
    }

    public long getTransactTime() {
        return transactTime;
    }

    public OrderResponse setStatus(OrderStatus status) {
        this.status = status;
        return this;
    }
}
