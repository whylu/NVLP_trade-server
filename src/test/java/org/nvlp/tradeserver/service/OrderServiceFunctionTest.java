package org.nvlp.tradeserver.service;

import org.junit.jupiter.api.Test;
import org.nvlp.tradeserver.model.DepositRequest;
import org.nvlp.tradeserver.model.PlaceOrderRequest;
import org.nvlp.tradeserver.model.enumn.OrderType;
import org.nvlp.tradeserver.model.enumn.Side;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class OrderServiceFunctionTest {

    @SpyBean
    @Autowired
    private OrderService orderService;

    @SpyBean
//    @Autowired
    private WalletService walletService;

    @SpyBean
    private TradeService tradeService;

    @DirtiesContext
    @Test
    void test_realize_fill_order() {
        DepositRequest depositRequest1 = new DepositRequest(1, 1000, "USD");
        walletService.deposit(depositRequest1);
        PlaceOrderRequest buyOrderRequest = createPlaceOrderRequest(1, 100.0d, BigDecimal.valueOf(1d), Side.BUY);
        orderService.placeOrder(buyOrderRequest);

        walletService.deposit(new DepositRequest(2, 10, "BTC"));
        PlaceOrderRequest sellOrderRequest = createPlaceOrderRequest(2, 100d, BigDecimal.valueOf(1d), Side.SELL);
        orderService.placeOrder(sellOrderRequest);

        assertThat(walletService.getWallet(1, "USD").getAmount()).isEqualByComparingTo(BigDecimal.valueOf(900d));
        assertThat(walletService.getWallet(1, "BTC").getAmount()).isEqualByComparingTo(BigDecimal.valueOf(1d));
        assertThat(walletService.getWallet(2, "USD").getAmount()).isEqualByComparingTo(BigDecimal.valueOf(100d));
        assertThat(walletService.getWallet(2, "BTC").getAmount()).isEqualByComparingTo(BigDecimal.valueOf(9d));

        // takerId: 2, takerCurrency: BTC, takerAmount: 1
        // makerId: 1, makerCurrency: USD, makerAmount: 100
        verify(walletService, times(1)).swapFrozenAndRealizeAmount(
                eq(2), eq("BTC"), argThat(v-> v.compareTo(BigDecimal.valueOf(1d))==0),
                eq(1), eq("USD"), argThat(v-> v.compareTo(BigDecimal.valueOf(100d))==0));
    }



    @DirtiesContext
    @Test
    void test_self_trade() {
        walletService.deposit(new DepositRequest(1, 1000, "USD"));
        orderService.placeOrder(createPlaceOrderRequest(1, 100d, BigDecimal.valueOf(1d), Side.BUY));

        walletService.deposit(new DepositRequest(1, 10, "BTC"));
        orderService.placeOrder(createPlaceOrderRequest(1, 100d, BigDecimal.valueOf(1d), Side.SELL));

        assertThat(walletService.getWallet(1, "USD").getAmount()).isEqualByComparingTo(BigDecimal.valueOf(1000d));
        assertThat(walletService.getWallet(1, "BTC").getAmount()).isEqualByComparingTo(BigDecimal.valueOf(10d));

        // takerId: 1, takerCurrency: BTC, takerAmount: 1
        // makerId: 1, makerCurrency: USD, makerAmount: 100
        verify(walletService, times(1)).swapFrozenAndRealizeAmount(
                eq(1), eq("BTC"), argThat(v-> v.compareTo(BigDecimal.valueOf(1d))==0),
                eq(1), eq("USD"), argThat(v-> v.compareTo(BigDecimal.valueOf(100d))==0));
    }

    private PlaceOrderRequest createPlaceOrderRequest(int userId, double price, BigDecimal size, Side side) {
        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setUserId(userId);
        request.setSize(size);
        request.setPrice(price);
        request.setSymbol("BTC-USD");
        request.setSide(side.name());
        request.setType(OrderType.LIMIT.name());
        return request;
    }

}
