package org.nvlp.tradeserver.model;

import java.math.BigDecimal;

public class Wallet {
    private BigDecimal amount = BigDecimal.ZERO;
    private BigDecimal frozen = BigDecimal.ZERO; // pending order's amount
    private String currency;
    private int userId;

    public Wallet() {}
    public Wallet(int userId, String currency) {
        this.currency = currency;
        this.userId = userId;
    }

    public BigDecimal getFrozen() {
        return frozen;
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

    public synchronized void addAmount(BigDecimal increment) {
        amount = amount.add(increment);
    }
    public void addAmount(double increment) {
        addAmount(BigDecimal.valueOf(increment));
    }

    /**
     * return null if insufficient balance, return wallet remain amount if success
     * @param subtrahend
     * @return
     */
    public BigDecimal decreaseAmount(double subtrahend) {
        return decreaseAmount(BigDecimal.valueOf(subtrahend));
    }
    public synchronized BigDecimal decreaseAmount(BigDecimal subtrahend) {
        BigDecimal result = null;
        synchronized (this) {
            if(this.amount.compareTo(subtrahend)>=0) {
                this.amount = this.amount.subtract(subtrahend);
                result = this.amount;
            }
        }
        return result; // don't return this.amount, out of synchronized block may change value
    }


    /**
     * return null if insufficient balance, return wallet remain amount if success
     * @param freezingAmount
     * @return
     */
    public BigDecimal freeze(BigDecimal freezingAmount) {
        BigDecimal result = null;
        synchronized (this) {
            if(this.amount.compareTo(freezingAmount)>=0) {
                this.amount = this.amount.subtract(freezingAmount);
                this.frozen = this.frozen.add(freezingAmount);
                result = this.amount;
            }
        }
        return result;
    }


    /**
     * return null if insufficient balance, return wallet remain amount if success
     * @param unfreezingAmount
     * @return
     */
    public BigDecimal unfreeze(BigDecimal unfreezingAmount) {
        BigDecimal result = null;
        synchronized (this) {
            if(this.frozen.compareTo(unfreezingAmount)>=0) {
                this.frozen = this.frozen.subtract(unfreezingAmount);
                this.amount = this.amount.add(unfreezingAmount);
                result = this.amount;
            }
        }
        return result;
    }

    /**
     * return null if insufficient balance, return wallet remain frozen if success
     * @param cutOffAmount
     * @return
     */
    public BigDecimal cutOffFrozen(BigDecimal cutOffAmount) {
        BigDecimal result = null;
        synchronized (this) {
            if(this.frozen.compareTo(cutOffAmount)>=0) {
                this.frozen = this.frozen.subtract(cutOffAmount);
                result = this.frozen;
            }
        }
        return result;
    }
}
