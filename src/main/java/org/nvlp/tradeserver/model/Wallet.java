package org.nvlp.tradeserver.model;

import java.math.BigDecimal;

public class Wallet {
    private BigDecimal amount = BigDecimal.ZERO;
    private String currency;
    private int userId;

    public Wallet() {}
    public Wallet(int userId, String currency) {
        this.currency = currency;
        this.userId = userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public int getUserId() {
        return userId;
    }

    public void addAmount(BigDecimal increment) {
        amount = amount.add(increment);
    }
    public void addAmount(double increment) {
        addAmount(BigDecimal.valueOf(increment));
    }
}
