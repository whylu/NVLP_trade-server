package org.nvlp.tradeserver.model;

import org.nvlp.tradeserver.model.enumn.OrderStatus;
import org.nvlp.tradeserver.model.enumn.OrderType;
import org.nvlp.tradeserver.model.enumn.Side;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class OrderResponse {
    private String symbol;
    private long orderId;
    private OrderStatus status;
    private OrderType type;
    private Side side;
    private double price;    // original price
    private BigDecimal size; // original size
    private long transactTime;  // the time order handle by trade server
    private List<FilledOrder> filledOrders;
    private ErrorCode errorCode;


    public static OrderResponse of(PlaceOrderRequest request) {
        OrderResponse r = new OrderResponse();
        r.symbol = request.getSymbol();
        r.size = request.getSize();
        r.price = request.getPrice();
        r.type = request.getType();
        r.side = request.getSide();
        return r;
    }

    public List<FilledOrder> getFilledOrders() {
        return filledOrders;
    }

    public String getSymbol() {
        return symbol;
    }

    public long getOrderId() {
        return orderId;
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

    public BigDecimal getSize() {
        return size;
    }

    public long getTransactTime() {
        return transactTime;
    }

    public int getErrorCode() {
        return errorCode==null? 0 : errorCode.getCode();
    }

    public OrderResponse reject(ErrorCode errorCode) {
        status = OrderStatus.REJECTED;
        transactTime = Instant.now().toEpochMilli();
        this.errorCode = errorCode;
        return this;
    }

    public boolean isRejected() {
        return status == OrderStatus.REJECTED;
    }

    public OrderResponse compose(OrderInsertResult orderInsertResult) {
        orderId = orderInsertResult.getOrderId();
        List<FilledOrder> filledOrderList = orderInsertResult.getFilledOrderList();
        if(!filledOrderList.isEmpty()) { // partially or full transacted
            status = size.compareTo(orderInsertResult.getFilledSize())==0? OrderStatus.FILLED : OrderStatus.PARTIALLY_FILLED;
        } else { // no transacted
            status = OrderStatus.INSERTED;
        }
        transactTime = Instant.now().toEpochMilli();
        filledOrders = orderInsertResult.getFilledOrderList();
        return this;
    }
}
