package org.nvlp.tradeserver.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class WalletTest {

    @Test
    void decreaseAmount() {
        Wallet w = new Wallet();
        w.addAmount(10000);
        BigDecimal result = w.decreaseAmount(1234.5678);
        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(8765.4322));

        result = w.decreaseAmount(50000);
        assertThat(result).isNull();

        assertThat(w.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(8765.4322));

    }

    @Test
    void freeze() {
        Wallet w = new Wallet();
        w.addAmount(10000);
        BigDecimal result = w.freeze(BigDecimal.valueOf(1234.5678));
        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(8765.4322));

        assertThat(w.getFrozen()).isEqualByComparingTo(BigDecimal.valueOf(1234.5678));

        result = w.freeze(BigDecimal.valueOf(50000));
        assertThat(result).isNull();
    }
}