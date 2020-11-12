package org.nvlp.tradeserver.test.utils;

import org.nvlp.tradeserver.model.OrderResponse;
import org.nvlp.tradeserver.model.PlaceOrderRequest;

import java.util.Random;

import static org.assertj.core.api.Assertions.*;

public class TestUtils {
    private static Random random = new Random();
    public static int randomUid() {
        return random.nextInt();
    }

    public static void assertThatInputValueUnchanged(PlaceOrderRequest request, OrderResponse orderResponse) {
        assertThat(orderResponse.getSize()).isEqualTo(request.getSize());
        assertThat(orderResponse.getPrice()).isEqualTo(request.getPrice());
        assertThat(orderResponse.getSymbol()).isEqualTo(request.getSymbol());
        assertThat(orderResponse.getSide()).isEqualTo(request.getSide());
        assertThat(orderResponse.getType()).isEqualTo(request.getType());
    }
}
