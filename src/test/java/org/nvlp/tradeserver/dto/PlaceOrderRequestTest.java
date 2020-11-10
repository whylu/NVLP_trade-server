package org.nvlp.tradeserver.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class PlaceOrderRequestTest {

    @Test
    void getBase() {
        PlaceOrderRequest r = new PlaceOrderRequest();
        r.setSymbol("BTC-USD");
        assertThat(r.getBase()).isEqualTo("BTC");
        assertThat(r.getQuote()).isEqualTo("USD");
    }
}