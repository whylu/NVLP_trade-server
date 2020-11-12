package org.nvlp.tradeserver.service;

import org.assertj.core.api.*;
import org.junit.jupiter.api.Test;
import org.nvlp.tradeserver.model.OrderResponse;
import org.nvlp.tradeserver.model.PlaceOrderRequest;
import org.nvlp.tradeserver.model.enumn.OrderStatus;
import org.nvlp.tradeserver.model.enumn.OrderType;
import org.nvlp.tradeserver.model.enumn.Side;
import org.nvlp.tradeserver.test.utils.TestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TradeServiceTest {

    @Autowired
    private TradeService tradeService;

    @Test
    public void testInit() throws NoSuchFieldException, IllegalAccessException {
        Field marketOrderBooksField = TradeService.class.getDeclaredField("marketOrderBooks");
        marketOrderBooksField.setAccessible(true);
        Map map = (Map) marketOrderBooksField.get(tradeService);
        assertThat(map).isNotEmpty();
    }

    @Test
    void place_inserted() {
        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setUserId(TestUtils.randomUid());
        request.setSize(10);
        request.setPrice(9999.99);
        request.setSymbol("BTC-USD");
        request.setSide(Side.BUY.name());
        request.setType(OrderType.LIMIT.name());
        OrderResponse response = tradeService.place(request);
        TestUtils.assertThatInputValueUnchanged(request, response);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.INSERTED);
        assertThat(response.getTransactTime()).isNotZero();

        Map<Double, BigDecimal> bids = tradeService.getBids("BTC-USD");
        BigDecimal size = bids.get(9999.99);
        assertThat(size).isEqualByComparingTo(BigDecimal.valueOf(10));

        response = tradeService.place(request);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.INSERTED);
        assertThat(response.getTransactTime()).isNotZero();
        size = bids.get(9999.99);
        assertThat(size).isEqualByComparingTo(BigDecimal.valueOf(20));
    }
}