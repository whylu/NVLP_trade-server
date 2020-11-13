package org.nvlp.tradeserver.model;

import org.junit.jupiter.api.Test;
import org.nvlp.tradeserver.model.enumn.Side;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class PendingOrderTest {

    @Test
    void transact() {
        PendingOrder pendingOrder = new PendingOrder(1, 200, BigDecimal.valueOf(10), Side.BUY);
        assertThat(pendingOrder.getOrigSize()).isEqualByComparingTo(BigDecimal.valueOf(10));
        assertThat(pendingOrder.getSize()).isEqualByComparingTo(BigDecimal.valueOf(10));

        FilledOrder filledOrder = pendingOrder.transact(BigDecimal.valueOf(3));
        assertThat(filledOrder.getSize()).isEqualByComparingTo(BigDecimal.valueOf(3));
        assertThat(filledOrder.getOrigSize()).isEqualByComparingTo(BigDecimal.valueOf(10));

        assertThat(pendingOrder.getOrigSize()).isEqualByComparingTo(BigDecimal.valueOf(10));
        assertThat(pendingOrder.getSize()).isEqualByComparingTo(BigDecimal.valueOf(7)); //remain
    }
}