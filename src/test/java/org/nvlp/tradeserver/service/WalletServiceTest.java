package org.nvlp.tradeserver.service;

import org.junit.jupiter.api.Test;
import org.nvlp.tradeserver.dto.DepositRequest;
import org.nvlp.tradeserver.model.Wallet;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class WalletServiceTest {

    private WalletService service = new WalletService();

    @Test
    void transfer() {
        int fromUserId = 100;
        int toUserId = 101;
        String currency = "USD";
        service.deposit(new DepositRequest(fromUserId, 10000, currency));

        boolean success = service.transfer(currency, BigDecimal.valueOf(123.456), fromUserId, toUserId);
        assertThat(success).isTrue();

        assertThat(service.getWallet(toUserId, currency).getAmount())
                .isEqualByComparingTo(BigDecimal.valueOf(123.456));

        assertThat(service.getWallet(fromUserId, currency).getAmount())
                .isEqualByComparingTo(BigDecimal.valueOf(9876.544));


    }
}