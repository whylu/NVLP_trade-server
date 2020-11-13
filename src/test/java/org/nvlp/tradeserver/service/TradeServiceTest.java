package org.nvlp.tradeserver.service;

import org.junit.jupiter.api.Test;
import org.nvlp.tradeserver.model.*;
import org.nvlp.tradeserver.model.enumn.OrderStatus;
import org.nvlp.tradeserver.model.enumn.OrderType;
import org.nvlp.tradeserver.model.enumn.Side;
import org.nvlp.tradeserver.test.utils.TestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class TradeServiceTest {

    @Autowired
    private TradeService tradeService;

    @Autowired
    private WalletService walletService;

    @Test
    public void testInit() throws NoSuchFieldException, IllegalAccessException {
        Field marketOrderBooksField = TradeService.class.getDeclaredField("marketOrderBooks");
        marketOrderBooksField.setAccessible(true);
        Map map = (Map) marketOrderBooksField.get(tradeService);
        assertThat(map).isNotEmpty();
    }

    @DirtiesContext
    @Test
    void place_inserted() {
        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setUserId(TestUtils.randomUid());
        request.setSize(BigDecimal.valueOf(10d));
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
        assertThat(size).isEqualByComparingTo(BigDecimal.valueOf(10d));

        response = tradeService.place(request);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.INSERTED);
        assertThat(response.getTransactTime()).isNotZero();
        size = bids.get(9999.99);
        assertThat(size).isEqualByComparingTo(BigDecimal.valueOf(20d));


        request = new PlaceOrderRequest();
        request.setUserId(TestUtils.randomUid());
        request.setSize(BigDecimal.valueOf(0.1));
        request.setPrice(50050.503);
        request.setSymbol("BTC-USD");
        request.setSide(Side.SELL.name());
        request.setType(OrderType.LIMIT.name());
        response = tradeService.place(request);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.INSERTED);
        assertThat(response.getTransactTime()).isNotZero();

        Map<Double, BigDecimal> asks = tradeService.getAsks("BTC-USD");
        assertThat(asks.get(50050.503)).isEqualByComparingTo(BigDecimal.valueOf(0.1));

        response = tradeService.place(request);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.INSERTED);
        assertThat(response.getTransactTime()).isNotZero();
        assertThat(asks.get(50050.503)).isEqualByComparingTo(BigDecimal.valueOf(0.2));
    }


    @DirtiesContext
    @Test
    void place_match_buy_with_sell_by_price() {
        for (double i=10; i<20; i++) {
            tradeService.place(createOrder(i, 1, Side.SELL));
        }
        // sell: 10, 11, 12, 13, 14, ..., 19

        // BUY: price 15, size 5
        // should match 10, 11, 12, 13, 14 in order
        OrderResponse response = tradeService.place(createOrder(14, 5, Side.BUY));
        List<FilledOrder> filledOrders = response.getFilledOrders();
        assertThat(filledOrders).hasSize(5);
        assertThat(filledOrders.get(0).getPrice()).isEqualTo(10);
        assertThat(filledOrders.get(1).getPrice()).isEqualTo(11);
        assertThat(filledOrders.get(2).getPrice()).isEqualTo(12);
        assertThat(filledOrders.get(3).getPrice()).isEqualTo(13);
        assertThat(filledOrders.get(4).getPrice()).isEqualTo(14);
        for (FilledOrder filledOrder : filledOrders) {
            assertThat(filledOrder.getSize()).isEqualTo(BigDecimal.valueOf(1.0));
            assertThat(filledOrder.getTimestamp()).isNotZero();
            assertThat(filledOrder.getId()).isNotZero();
            assertThat(filledOrder.getSide()).isEqualTo(Side.SELL);
        }
        assertThat(response.getStatus()).isEqualTo(OrderStatus.FILLED);

        // BUY 20 size 10
        // match 15, 16, 17, 18, 19, and result in partially filled
        response = tradeService.place(createOrder(20, 10, Side.BUY));
        assertThat(response.getStatus()).isEqualTo(OrderStatus.PARTIALLY_FILLED);
    }

    @DirtiesContext
    @Test
    void place_match_sell_with_buy_by_price() {
        for (double i=20; i>10; i--) {
            tradeService.place(createOrder(i, 1, Side.BUY));
        }
        // BUY: 20, 19, 18, 17, 16, 15...., 11

        // SELL: price 16, size 5
        OrderResponse response = tradeService.place(createOrder(16, 5, Side.SELL));
        List<FilledOrder> filledOrders = response.getFilledOrders();
        assertThat(filledOrders).hasSize(5);
        assertThat(filledOrders.get(0).getPrice()).isEqualTo(20);
        assertThat(filledOrders.get(1).getPrice()).isEqualTo(19);
        assertThat(filledOrders.get(2).getPrice()).isEqualTo(18);
        assertThat(filledOrders.get(3).getPrice()).isEqualTo(17);
        assertThat(filledOrders.get(4).getPrice()).isEqualTo(16);
        for (FilledOrder filledOrder : filledOrders) {
            assertThat(filledOrder.getSize()).isEqualTo(BigDecimal.valueOf(1d));
            assertThat(filledOrder.getTimestamp()).isNotZero();
            assertThat(filledOrder.getId()).isNotZero();
            assertThat(filledOrder.getSide()).isEqualTo(Side.BUY);
        }
        assertThat(response.getStatus()).isEqualTo(OrderStatus.FILLED);


        // BUY 1 size 10
        // and result in partially filled
        response = tradeService.place(createOrder(1, 10, Side.SELL));
        assertThat(response.getStatus()).isEqualTo(OrderStatus.PARTIALLY_FILLED);
    }


    @DirtiesContext
    @Test
    void place_match_buy_with_sell_by_size() {
        for (int i=0; i<10; i++) {
            tradeService.place(createOrder(500, 0.1, Side.SELL));
        }
        assertThat(tradeService.getAsks("BTC-USD").get(500d)).isEqualByComparingTo(BigDecimal.valueOf(1d));
        // SELL price=500,
        //      size = 0.1, 0.1, 0.1, ..., 0.1   , total = 1

        OrderResponse response = tradeService.place(createOrder(505, 0.5, Side.BUY));
        List<FilledOrder> filledOrders = response.getFilledOrders();
        assertThat(filledOrders).hasSize(5);
        assertThat(filledOrders.get(0).getSize()).isEqualTo(BigDecimal.valueOf(0.1));
        assertThat(filledOrders.get(1).getSize()).isEqualTo(BigDecimal.valueOf(0.1));
        assertThat(filledOrders.get(2).getSize()).isEqualTo(BigDecimal.valueOf(0.1));
        assertThat(filledOrders.get(3).getSize()).isEqualTo(BigDecimal.valueOf(0.1));
        assertThat(filledOrders.get(4).getSize()).isEqualTo(BigDecimal.valueOf(0.1));
        for (FilledOrder filledOrder : filledOrders) {
            assertThat(filledOrder.getPrice()).isEqualTo(500);
            assertThat(filledOrder.getTimestamp()).isNotZero();
            assertThat(filledOrder.getId()).isNotZero();
            assertThat(filledOrder.getSide()).isEqualTo(Side.SELL);
        }
        assertThat(response.getStatus()).isEqualTo(OrderStatus.FILLED);
        assertThat(tradeService.getAsks("BTC-USD").get(500d)).isEqualByComparingTo(BigDecimal.valueOf(0.5));

        // BUY the rest
        response = tradeService.place(createOrder(600, 1, Side.BUY));
        assertThat(response.getStatus()).isEqualTo(OrderStatus.PARTIALLY_FILLED);
        assertThat(tradeService.getAsks("BTC-USD")).isNotNull();
    }


    @DirtiesContext
    @Test
    void place_match_sell_with_buy_by_size() {
        for (int i=0; i<10; i++) {
            tradeService.place(createOrder(100, 0.1, Side.BUY));
        }
        assertThat(tradeService.getBids("BTC-USD").get(100d)).isEqualByComparingTo(BigDecimal.valueOf(1d));

        // SELL price=500,
        //      size = 0.1, 0.1, 0.1, ..., 0.1   , total = 1
        OrderResponse response = tradeService.place(createOrder(50, 0.5, Side.SELL));
        List<FilledOrder> filledOrders = response.getFilledOrders();
        assertThat(filledOrders).hasSize(5);
        assertThat(filledOrders.get(0).getSize()).isEqualTo(BigDecimal.valueOf(0.1));
        assertThat(filledOrders.get(1).getSize()).isEqualTo(BigDecimal.valueOf(0.1));
        assertThat(filledOrders.get(2).getSize()).isEqualTo(BigDecimal.valueOf(0.1));
        assertThat(filledOrders.get(3).getSize()).isEqualTo(BigDecimal.valueOf(0.1));
        assertThat(filledOrders.get(4).getSize()).isEqualTo(BigDecimal.valueOf(0.1));
        for (FilledOrder filledOrder : filledOrders) {
            assertThat(filledOrder.getPrice()).isEqualTo(100);
            assertThat(filledOrder.getTimestamp()).isNotZero();
            assertThat(filledOrder.getId()).isNotZero();
            assertThat(filledOrder.getSide()).isEqualTo(Side.BUY);
        }
        assertThat(response.getStatus()).isEqualTo(OrderStatus.FILLED);
        assertThat(tradeService.getBids("BTC-USD").get(100d)).isEqualByComparingTo(BigDecimal.valueOf(0.5));

        // BUY the rest
        response = tradeService.place(createOrder(100, 1, Side.SELL));
        assertThat(response.getStatus()).isEqualTo(OrderStatus.PARTIALLY_FILLED);
        assertThat(tradeService.getBids("BTC-USD")).isNotNull();
    }

    @DirtiesContext
    @Test
    void place_test_for_orderbook_consume() {
        tradeService.place(createOrder(100, 0.5, Side.SELL));

        OrderResponse response = tradeService.place(createOrder(100, 0.4, Side.BUY));
        assertThat(response.getStatus()).isEqualTo(OrderStatus.FILLED);

        response = tradeService.place(createOrder(100, 0.1, Side.BUY));
        assertThat(response.getStatus()).isEqualTo(OrderStatus.FILLED);

        response = tradeService.place(createOrder(100, 0.1, Side.BUY));
        assertThat(response.getStatus()).isEqualTo(OrderStatus.INSERTED);

    }

    private PlaceOrderRequest createOrder(double price, double size, Side side) {
        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setUserId(TestUtils.randomUid());
        request.setSize(BigDecimal.valueOf(size));
        request.setPrice(price);
        request.setSymbol("BTC-USD");
        request.setSide(side.name());
        request.setType(OrderType.LIMIT.name());
        return request;
    }

}