package org.nvlp.tradeserver.model;

public class MarketQuote {

    private double minPrice; // 0.5
    private double minPriceIncrement; // 0.5
    private double minSize;  // 0.0002
    private double minSizeIncrement; // 0.0001

    public double getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(double minPrice) {
        this.minPrice = minPrice;
    }

    public double getMinPriceIncrement() {
        return minPriceIncrement;
    }

    public void setMinPriceIncrement(double minPriceIncrement) {
        this.minPriceIncrement = minPriceIncrement;
    }

    public double getMinSize() {
        return minSize;
    }

    public void setMinSize(double minSize) {
        this.minSize = minSize;
    }

    public double getMinSizeIncrement() {
        return minSizeIncrement;
    }

    public void setMinSizeIncrement(double minSizeIncrement) {
        this.minSizeIncrement = minSizeIncrement;
    }
}