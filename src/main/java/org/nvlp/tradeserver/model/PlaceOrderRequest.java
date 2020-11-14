package org.nvlp.tradeserver.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.nvlp.tradeserver.model.enumn.OrderType;
import org.nvlp.tradeserver.model.enumn.Side;

import java.math.BigDecimal;

public class PlaceOrderRequest {
    private int userId;
    private String symbol; // BTC-USD
    private String type;
    private String side;
    private double price;
    private BigDecimal size;

    // derivative values
    private String[] base_quote;
    private BigDecimal volume;

    public int getUserId() {
        return userId;
    }

    public OrderType getType() {
        return OrderType.of(type);
    }

    public Side getSide() {
        return Side.of(side);
    }

    public double getPrice() {
        return price;
    }

    public BigDecimal getSize() {
        return size;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setSize(BigDecimal size) {
        this.size = size;
    }

    @JsonIgnore
    public String getBase() {
        if(base_quote==null) {
            parseBaseQuote();
        }
        return base_quote[0];
    }
    @JsonIgnore
    public String getQuote() {
        if(base_quote==null) {
            parseBaseQuote();
        }
        return base_quote[1];
    }

    private void parseBaseQuote() {
        base_quote = symbol.split("-");
    }

    @JsonIgnore
    public ErrorCode isValid() {
        if(userId <= 0)
            return ErrorCode.MISSING_USER_ID;
        if(type==null)
            return ErrorCode.MISSING_ORDER_TYPE;
        if(getType()==null)
            return ErrorCode.INVALID_ORDER_TYPE;
        if(getSide()==null)
            return ErrorCode.MISSING_ORDER_SIDE;
        if(price<=0)
            return ErrorCode.INVALID_ORDER_PRICE_ZERO;
        if(size==null)
            return ErrorCode.MISSING_ORDER_SIZE;
        if(size.compareTo(BigDecimal.ZERO)<=0)
            return ErrorCode.INVALID_ORDER_SIZE_ZERO;
        return null;
    }

    @JsonIgnore
    public BigDecimal getVolume() {
        if(volume==null) {
            volume = BigDecimal.valueOf(price).multiply(size);
        }
        return volume;
    }

    @JsonIgnore
    public String getFreezeCurrency() {
        return getSide()==Side.BUY? getQuote() : getBase();
    }

    @JsonIgnore
    public BigDecimal getFreezeAmount() {
        return getSide()==Side.BUY? getVolume() : size;
    }
}
