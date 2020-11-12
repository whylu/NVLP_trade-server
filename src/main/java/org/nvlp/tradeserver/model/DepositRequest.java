package org.nvlp.tradeserver.model;

import org.springframework.util.StringUtils;

public class DepositRequest {
    private double amount;
    private String currency;
    private int userId;

    public DepositRequest(){}
    public DepositRequest(int userId, double amount, String currency) {
        this.amount = amount;
        this.currency = currency;
        this.userId = userId;
    }

    public double getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public int getUserId() {
        return userId;
    }

    public boolean isValid() {
        if(StringUtils.isEmpty(currency) || amount<=0) {
            return false;
        }
        return true;
    }
}
