package org.nvlp.tradeserver.model;

import java.math.BigDecimal;

public class MarketQuote {

    private BigDecimal minPrice; // 0.5
    private BigDecimal minPriceIncrement; // 0.5
    private BigDecimal minSize;  // 0.0002
    private BigDecimal minSizeIncrement; // 0.0001

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMinPriceIncrement() {
        return minPriceIncrement;
    }

    public void setMinPriceIncrement(BigDecimal minPriceIncrement) {
        this.minPriceIncrement = minPriceIncrement;
    }

    public BigDecimal getMinSize() {
        return minSize;
    }

    public void setMinSize(BigDecimal minSize) {
        this.minSize = minSize;
    }

    public BigDecimal getMinSizeIncrement() {
        return minSizeIncrement;
    }

    public void setMinSizeIncrement(BigDecimal minSizeIncrement) {
        this.minSizeIncrement = minSizeIncrement;
    }
}