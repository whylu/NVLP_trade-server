package org.nvlp.tradeserver.service;

import org.junit.jupiter.api.Test;
import org.nvlp.tradeserver.model.PlaceOrderRequest;
import org.nvlp.tradeserver.model.OrderResponse;
import org.nvlp.tradeserver.model.enumn.OrderStatus;
import org.nvlp.tradeserver.model.enumn.OrderType;
import org.nvlp.tradeserver.model.enumn.Side;
import org.nvlp.tradeserver.test.utils.TestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class OrderServiceTest {

    @Autowired
    private OrderService service;

    @Test
    void placeOrder_insufficientBalance() {
        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setUserId(TestUtils.randomUid());
        request.setSize(1);
        request.setPrice(100000);
        request.setSymbol("BTC-USD");
        request.setSide(Side.BUY.name());
        request.setType(OrderType.LIMIT.name());

        OrderResponse orderResponse = service.placeOrder(request);
        assertThat(orderResponse.getSize()).isEqualTo(1);
        assertThat(orderResponse.getPrice()).isEqualTo(100000);
        assertThat(orderResponse.getSymbol()).isEqualTo("BTC-USD");
        assertThat(orderResponse.getSide()).isEqualTo(Side.BUY);
        assertThat(orderResponse.getType()).isEqualTo(OrderType.LIMIT);
        assertThat(orderResponse.getStatus()).isEqualTo(OrderStatus.REJECTED);
        assertThat(orderResponse.getTransactTime()).isNotZero();
    }
}