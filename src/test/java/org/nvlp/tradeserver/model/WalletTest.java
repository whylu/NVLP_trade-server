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

        result = w.decreaseAmount(8765.4322);
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
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

        result = w.freeze(BigDecimal.valueOf(8765.4322));
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);

    }

    @Test
    void unfreeze() {
        Wallet wallet = new Wallet();
        wallet.addAmount(10000);
        wallet.freeze(BigDecimal.valueOf(10000));

        BigDecimal unfreeze = wallet.unfreeze(BigDecimal.valueOf(50000));
        assertThat(unfreeze).isNull();

        BigDecimal walletAmount = wallet.unfreeze(BigDecimal.valueOf(500));
        assertThat(walletAmount).isEqualByComparingTo(BigDecimal.valueOf(500));
        assertThat(wallet.getFrozen()).isEqualByComparingTo(BigDecimal.valueOf(9500));

        walletAmount = wallet.unfreeze(BigDecimal.valueOf(9500));
        assertThat(walletAmount).isEqualByComparingTo(BigDecimal.valueOf(10000));
        assertThat(wallet.getFrozen()).isEqualByComparingTo(BigDecimal.ZERO);
    }

}