package org.nvlp.tradeserver.service;

import org.junit.jupiter.api.Test;
import org.nvlp.tradeserver.dto.PlaceOrderRequest;
import org.nvlp.tradeserver.model.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OrderValidationServiceTest {

    @Autowired
    private OrderValidationService service;


//    markets.base.BTC.quote.USD.minPrice=0.5
//    markets.base.BTC.quote.USD.minPriceIncrement=0.5
//    markets.base.BTC.quote.USD.minSize=0.0002
//    markets.base.BTC.quote.USD.minSizeIncrement=0.0001
    @Test
    void validate() {
        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setSymbol("BTC-USD");
        request.setPrice(0.1);
        assertThat(service.validate(request)).isEqualTo(ErrorCode.INVALID_ORDER_PRICE_MIN);

        request.setPrice(0.6);
        assertThat(service.validate(request)).isEqualTo(ErrorCode.INVALID_ORDER_PRICE_INCREMENT);
        request.setPrice(2);

        request.setSize(0.00001);
        assertThat(service.validate(request)).isEqualTo(ErrorCode.INVALID_ORDER_SIZE_MIN);

        request.setSize(0.00025);
        assertThat(service.validate(request)).isEqualTo(ErrorCode.INVALID_ORDER_SIZE_INCREMENT);

        request.setSize(0.0002);
        assertThat(service.validate(request)).isNull();
        request.setSize(0.0003);
        assertThat(service.validate(request)).isNull();
        request.setSize(199.99);
        assertThat(service.validate(request)).isNull();

        request.setPrice(2000);
        assertThat(service.validate(request)).isNull();

    }
}