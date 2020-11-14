package org.nvlp.tradeserver.model.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OrderBook {
    // [[<price>, <size>], [[price, size], ...]
    private List<double[]> bids; // [[100, 0.1], [99, 0.1], ...]
    private List<double[]> asks; // [[105, 0.1], [106, 0.1], ...]

    public OrderBook(Map<Double, BigDecimal> bids, Map<Double, BigDecimal> asks) {
        this.bids = bids.entrySet().stream()
                .map(e->new double[]{e.getKey().doubleValue(), e.getValue().doubleValue()})
                .collect(Collectors.toList());
        this.asks = asks.entrySet().stream()
                .map(e->new double[]{e.getKey().doubleValue(), e.getValue().doubleValue()})
                .collect(Collectors.toList());
    }

    public List<double[]> getBids() {
        return bids;
    }

    public List<double[]> getAsks() {
        return asks;
    }
}
