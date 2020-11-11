package org.nvlp.tradeserver.dto;

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
    private double size;

    private String[] base_quote;

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

    public double getSize() {
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

    public void setSize(double size) {
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
    public boolean isValid() {
        if(userId <= 0)
            return false;
        if(getType()==null)
            return false;
        if(getSide()==null)
            return false;
        if(price<=0)
            return false;
        if(size<=0)
            return false;
        return true;
    }

    @JsonIgnore
    public BigDecimal getVolume() {
        return BigDecimal.valueOf(price).multiply(BigDecimal.valueOf(size));
    }

    @JsonIgnore
    public String freezeCurrency() {
        return getSide()==Side.BUY? getQuote() : getBase();
    }

    @JsonIgnore
    public BigDecimal freezeAmount() {
        return getSide()==Side.BUY? getVolume() : BigDecimal.valueOf(size);
    }
}
