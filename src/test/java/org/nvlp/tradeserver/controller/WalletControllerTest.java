package org.nvlp.tradeserver.controller;

import org.junit.jupiter.api.Test;
import org.nvlp.tradeserver.model.DepositRequest;
import org.nvlp.tradeserver.model.Wallet;
import org.nvlp.tradeserver.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.*;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getWallet() throws Exception {
        mockMvc.perform(get("/wallet/1/USD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is(1)))
                .andExpect(jsonPath("$.currency", is("USD")))
        ;
    }



    @Test
    public void deposit() throws Exception {
        int userId = 1;
        double depositAmount = 1000;

        MvcResult mvcResult = mockMvc.perform(get("/wallet/1/USDT")).andReturn();

        Wallet wallet = JsonUtils.parse(mvcResult.getResponse().getContentAsByteArray(), Wallet.class);
        BigDecimal amount = wallet.getAmount();


        DepositRequest request = new DepositRequest(userId, depositAmount, "USDT");
        mockMvc.perform(
                post("/wallet/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toString(request))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId", is(userId)))
        .andExpect(jsonPath("$.currency", is("USDT")))
        .andExpect(jsonPath("$.amount", is(amount.doubleValue() + depositAmount )))
        ;
    }


    @Test
    public void deposit_test_multiTread() throws Exception {
        int userId = 1;
        double depositAmount = 1000;
        int threadNumber = 500;
        Thread[] threads = new Thread[500];

        MvcResult mvcResult = mockMvc.perform(get("/wallet/1/USDT")).andReturn();
        Wallet wallet = JsonUtils.parse(mvcResult.getResponse().getContentAsByteArray(), Wallet.class);
        BigDecimal amount = wallet.getAmount();

        DepositRequest request = new DepositRequest(userId, depositAmount, "USDT");
        for (int i = 0; i < threadNumber; i++) {
            Thread thread = new Thread(() -> {
                try {
                    mockMvc.perform(
                            post("/wallet/deposit")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(JsonUtils.toString(request))
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            threads[i] = thread;
        }
        for (int i = 0; i < threadNumber; i++) {
            threads[i].start();
        }
        for (int i = 0; i < threadNumber; i++) {
            threads[i].join();
        }

        double totalIncrement = depositAmount * threadNumber;
        mockMvc.perform(get("/wallet/1/USDT"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId", is(userId)))
        .andExpect(jsonPath("$.currency", is("USDT")))
        .andExpect(jsonPath("$.amount", is(amount.doubleValue() + totalIncrement )))
        ;
    }


    @Test
    public void deposit_badAmount() throws Exception {
        mockMvc.perform(
                post("/wallet/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                  "amount": -100,
                  "currency": "USD",
                  "userId": 1
                }
                """)
        )
        .andExpect(status().isBadRequest())
        .andExpect(content().string("invalid deposit request body"))
        .andReturn();
    }

    @Test
    public void deposit_badCurrency() throws Exception {
        mockMvc.perform(
                post("/wallet/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "amount": 1100,
                  "currency": "",
                  "userId": 1
                }
                """)
        )
        .andExpect(status().isBadRequest())
        .andExpect(content().string("invalid deposit request body"))
        .andReturn();
    }

}