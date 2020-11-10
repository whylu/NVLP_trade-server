package org.nvlp.tradeserver.controller;

import org.junit.jupiter.api.Test;
import org.nvlp.tradeserver.dto.PlaceOrderRequest;
import org.nvlp.tradeserver.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.*;

import static org.junit.jupiter.api.Assertions.*;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @Test
    void placeOrder_bad_request_value() throws Exception {
        PlaceOrderRequest request = new PlaceOrderRequest();
        mockMvc.perform(post("/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toString(request)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "userId": 1,
                        "type": "MARKET", 
                        "side": "BUY",
                        "price": 1,
                        "size": 1
                    }
                    """))
                .andExpect(status().isBadRequest())
        .andDo(print());
    }


    @Test
    void placeOrder_invalid_request() throws Exception {
        PlaceOrderRequest request = new PlaceOrderRequest();

        mockMvc.perform(post("/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "userId": 1,
                        "symbol": "XMR-USD",
                        "type": "LIMIT", 
                        "side": "BUY",
                        "price": 1,
                        "size": 1
                    }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(is("invalid market")))
        ;
    }

}