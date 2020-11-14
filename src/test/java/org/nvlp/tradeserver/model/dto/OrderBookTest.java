package org.nvlp.tradeserver.model.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import static org.assertj.core.api.Assertions.*;

class OrderBookTest {

    @Test
    void getBids() {
        Map<Double, BigDecimal> bids = new ConcurrentSkipListMap(Comparator.reverseOrder());
        bids.put(100d, BigDecimal.ONE);
        bids.put(99d, BigDecimal.ONE);
        bids.put(98d, BigDecimal.ONE);

        Map<Double, BigDecimal> asks = new ConcurrentSkipListMap();
        asks.put(105d, BigDecimal.ONE);
        asks.put(106d, BigDecimal.ONE);
        asks.put(107d, BigDecimal.ONE);

        OrderBook orderBook = new OrderBook(bids, asks);
        assertThat(orderBook.getBids().get(0)[0]).isEqualTo(100d);
        assertThat(orderBook.getBids().get(1)[0]).isEqualTo(99d);
        assertThat(orderBook.getBids().get(2)[0]).isEqualTo(98d);

        assertThat(orderBook.getAsks().get(0)[0]).isEqualTo(105d);
        assertThat(orderBook.getAsks().get(1)[0]).isEqualTo(106d);
        assertThat(orderBook.getAsks().get(2)[0]).isEqualTo(107d);
    }
}