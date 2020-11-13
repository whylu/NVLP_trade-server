package org.nvlp.tradeserver.model;

import org.junit.jupiter.api.Test;
import org.nvlp.tradeserver.model.enumn.Side;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class FilledOrderTest {

    @Test
    void getCutOffFrozenAmount() {
        PendingOrder pendingOrder = new PendingOrder(1, 100.1, BigDecimal.valueOf(0.3), Side.BUY);
        FilledOrder filledOrder = pendingOrder.fullyTransact();
        // when this buy order fully transact, user get '0.3' of base currency
        assertThat(filledOrder.getRealizeAmount()).isEqualByComparingTo(BigDecimal.valueOf(0.3));
        assertThat(filledOrder.getCutOffFrozenAmount()).isEqualByComparingTo(BigDecimal.valueOf(0.3).multiply(BigDecimal.valueOf(100.1)));


        pendingOrder = new PendingOrder(1, 100.1, BigDecimal.valueOf(0.3), Side.SELL);
        filledOrder = pendingOrder.fullyTransact();
        // when this sell order fully transact, user get '0.3 * 100.1' of quote currency
        assertThat(filledOrder.getRealizeAmount()).isEqualByComparingTo(BigDecimal.valueOf(0.3).multiply(BigDecimal.valueOf(100.1)));
        assertThat(filledOrder.getCutOffFrozenAmount()).isEqualByComparingTo(BigDecimal.valueOf(0.3));
    }

}