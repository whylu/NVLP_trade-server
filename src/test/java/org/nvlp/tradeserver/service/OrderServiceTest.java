package org.nvlp.tradeserver.service;

import org.junit.jupiter.api.Test;
import org.nvlp.tradeserver.model.DepositRequest;
import org.nvlp.tradeserver.model.PlaceOrderRequest;
import org.nvlp.tradeserver.model.OrderResponse;
import org.nvlp.tradeserver.model.enumn.OrderStatus;
import org.nvlp.tradeserver.model.enumn.OrderType;
import org.nvlp.tradeserver.model.enumn.Side;
import org.nvlp.tradeserver.test.utils.TestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SpringBootTest
@DirtiesContext
class OrderServiceTest {

    @Autowired
    private OrderService service;

    @MockBean
    private TradeService tradeService;

    @SpyBean
    private WalletService walletService;

    @Test
    void placeOrder_insufficientBalance() {
        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setUserId(TestUtils.randomUid());
        request.setSize(BigDecimal.ONE);
        request.setPrice(100000);
        request.setSymbol("BTC-USD");
        request.setSide(Side.BUY.name());
        request.setType(OrderType.LIMIT.name());

        OrderResponse orderResponse = service.placeOrder(request);
        TestUtils.assertThatInputValueUnchanged(request, orderResponse);
        assertThat(orderResponse.getStatus()).isEqualTo(OrderStatus.REJECTED);
        assertThat(orderResponse.getTransactTime()).isNotZero();
    }


    @Test
    void placeOrder_reject_by_tradeService() {
        int userId = TestUtils.randomUid();
        walletService.deposit(new DepositRequest(userId, 10000, "USD"));

        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setUserId(userId);
        request.setSize(BigDecimal.ONE);
        request.setPrice(1000);
        request.setSymbol("BTC-USD");
        request.setSide(Side.BUY.name());
        request.setType(OrderType.LIMIT.name());

        OrderResponse fakeReject = OrderResponse.of(request).reject();
        doReturn(fakeReject).when(tradeService).place(request);

        OrderResponse orderResponse = service.placeOrder(request);
        TestUtils.assertThatInputValueUnchanged(request, orderResponse);

        verify(walletService).freeze(request.getFreezeCurrency(), request.getFreezeAmount(), request.getUserId());
        verify(walletService).unfreeze(request.getFreezeCurrency(), request.getFreezeAmount(), request.getUserId());
    }
}