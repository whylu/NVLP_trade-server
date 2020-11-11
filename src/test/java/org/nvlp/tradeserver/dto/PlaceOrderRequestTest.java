package org.nvlp.tradeserver.dto;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class PlaceOrderRequestTest {


    @Test
    public void getBase() {
        PlaceOrderRequest r = new PlaceOrderRequest();
        r.setSymbol("BTC-USD");
        assertThat(r.getBase()).isEqualTo("BTC");
        assertThat(r.getQuote()).isEqualTo("USD");
    }
}