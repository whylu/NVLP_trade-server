package org.nvlp.tradeserver.controller;

import org.junit.jupiter.api.Test;
import org.nvlp.tradeserver.model.DepositRequest;
import org.nvlp.tradeserver.model.ErrorCode;
import org.nvlp.tradeserver.model.PlaceOrderRequest;
import org.nvlp.tradeserver.model.enumn.OrderStatus;
import org.nvlp.tradeserver.model.enumn.OrderType;
import org.nvlp.tradeserver.model.enumn.Side;
import org.nvlp.tradeserver.service.OrderService;
import org.nvlp.tradeserver.service.WalletService;
import org.nvlp.tradeserver.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.*;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderService orderService;
    @Autowired
    private WalletService walletService;

    @Test
    void placeOrder_bad_request_value() throws Exception {
        PlaceOrderRequest request = new PlaceOrderRequest();
        mockMvc.perform(post("/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(OrderStatus.REJECTED.name())))
        ;

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
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(OrderStatus.REJECTED.name())))
                .andExpect(jsonPath("$.errorCode", is(ErrorCode.INVALID_ORDER_TYPE.getCode())))
        ;
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
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode", is(ErrorCode.INVALID_MARKER.getCode())))
                .andExpect(jsonPath("$.status", is(OrderStatus.REJECTED.name())))
        ;
    }


    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    @Test
    void placeOrder_insufficient_balance() throws Exception {
        mockMvc.perform(post("/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "userId": 1,
                        "symbol": "BTC-USD",
                        "type": "LIMIT", 
                        "side": "BUY",
                        "price": 1000,
                        "size": 1
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.INSUFFICIENT_BALANCE.getCode()))
                ;
    }

    @DirtiesContext
    @Test
    void placeOrder() throws Exception {
        walletService.deposit(new DepositRequest(1, 1000, "USD"));

        mockMvc.perform(post("/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "userId": 1,
                        "symbol": "BTC-USD",
                        "type": "LIMIT", 
                        "side": "BUY",
                        "price": 1,
                        "size": 1
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").isNotEmpty())
                .andExpect(jsonPath("$.status").value("INSERTED"))
                .andExpect(jsonPath("$.price").value(1))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.side").value("BUY"))
                .andExpect(jsonPath("$.type").value("LIMIT"))
                .andExpect(jsonPath("$.symbol").value("BTC-USD"))

                .andExpect(jsonPath("$.rejected").doesNotExist())
                ;
    }



    @DirtiesContext
    @Test
    void placeOrder_filled() throws Exception {
        walletService.deposit(new DepositRequest(1, 1000, "USD"));

        mockMvc.perform(post("/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                   {"userId":1,"symbol":"BTC-USD","type":"LIMIT","side":"BUY","price":1,"size":0.1}
                   """));
        mockMvc.perform(post("/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                   {"userId":1,"symbol":"BTC-USD","type":"LIMIT","side":"BUY","price":1,"size":0.2}
                   """));

        walletService.deposit(new DepositRequest(2, 1000, "BTC"));
        mockMvc.perform(post("/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "userId": 2,
                        "symbol": "BTC-USD",
                        "type": "LIMIT", 
                        "side": "SELL",
                        "price": 1,
                        "size": 0.3
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").isNotEmpty())
                .andExpect(jsonPath("$.status").value("FILLED"))
                .andExpect(jsonPath("$.price").value(1))
                .andExpect(jsonPath("$.size").value(0.3))
                .andExpect(jsonPath("$.side").value("SELL"))
                .andExpect(jsonPath("$.type").value("LIMIT"))
                .andExpect(jsonPath("$.symbol").value("BTC-USD"))
                .andExpect(jsonPath("$.filledOrders").isArray())
                .andExpect(jsonPath("$.filledOrders", hasSize(2)))
                .andExpect(jsonPath("$.filledOrders[0].price").value(1))
                .andExpect(jsonPath("$.filledOrders[0].size").value(0.1))
                .andExpect(jsonPath("$.filledOrders[1].price").value(1))
                .andExpect(jsonPath("$.filledOrders[1].size").value(0.2))

                .andExpect(jsonPath("$.filledOrders[0].id").doesNotExist())
                .andExpect(jsonPath("$.filledOrders[0].cutOffFrozenAmount").doesNotExist())
                .andExpect(jsonPath("$.filledOrders[0].realizeAmount").doesNotExist())
                .andExpect(jsonPath("$.filledOrders[0].origSize").doesNotExist())






        ;


    }

    @DirtiesContext
    @Test
    void book() throws Exception {
        walletService.deposit(new DepositRequest(1, 1000, "USD"));
        orderService.placeOrder(createPlaceOrderRequest(1, 100, BigDecimal.valueOf(0.3), Side.BUY));
        orderService.placeOrder(createPlaceOrderRequest(1, 99, BigDecimal.valueOf(0.9), Side.BUY));
        orderService.placeOrder(createPlaceOrderRequest(1, 102, BigDecimal.valueOf(0.12), Side.BUY));
        orderService.placeOrder(createPlaceOrderRequest(1, 100, BigDecimal.valueOf(0.2), Side.BUY));

        mockMvc.perform(get("/order/book/BTC-USD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bids[0][0]").value(102))
                .andExpect(jsonPath("$.bids[0][1]").value(0.12))
                .andExpect(jsonPath("$.bids[1][0]").value(100))
                .andExpect(jsonPath("$.bids[1][1]").value(0.5))
                .andExpect(jsonPath("$.bids[2][0]").value(99))
                .andExpect(jsonPath("$.bids[2][1]").value(0.9))
        ;


        walletService.deposit(new DepositRequest(2, 100, "BTC"));
        orderService.placeOrder(createPlaceOrderRequest(2, 200, BigDecimal.valueOf(0.2), Side.SELL));
        orderService.placeOrder(createPlaceOrderRequest(2, 150, BigDecimal.valueOf(0.15), Side.SELL));
        orderService.placeOrder(createPlaceOrderRequest(2, 250, BigDecimal.valueOf(0.25), Side.SELL));
        orderService.placeOrder(createPlaceOrderRequest(2, 200, BigDecimal.valueOf(0.2), Side.SELL));

        mockMvc.perform(get("/order/book/BTC-USD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bids[0][0]").value(102))
                .andExpect(jsonPath("$.bids[0][1]").value(0.12))
                .andExpect(jsonPath("$.bids[1][0]").value(100))
                .andExpect(jsonPath("$.bids[1][1]").value(0.5))
                .andExpect(jsonPath("$.bids[2][0]").value(99))
                .andExpect(jsonPath("$.bids[2][1]").value(0.9))

                .andExpect(jsonPath("$.asks[0][0]").value(150))
                .andExpect(jsonPath("$.asks[0][1]").value(0.15))
                .andExpect(jsonPath("$.asks[1][0]").value(200))
                .andExpect(jsonPath("$.asks[1][1]").value(0.4))
                .andExpect(jsonPath("$.asks[2][0]").value(250))
                .andExpect(jsonPath("$.asks[2][1]").value(0.25))
        ;

        orderService.placeOrder(createPlaceOrderRequest(2, 102, BigDecimal.valueOf(0.1), Side.SELL));
        mockMvc.perform(get("/order/book/BTC-USD"))
                .andExpect(jsonPath("$.bids[0][0]").value(102))
                .andExpect(jsonPath("$.bids[0][1]").value(0.02))
        ;
        orderService.placeOrder(createPlaceOrderRequest(2, 102, BigDecimal.valueOf(0.02), Side.SELL));
        mockMvc.perform(get("/order/book/BTC-USD"))
                .andExpect(jsonPath("$.bids[0][0]").value(100))
                .andExpect(jsonPath("$.bids[0][1]").value(0.5))
        ;

        orderService.placeOrder(createPlaceOrderRequest(2, 100, BigDecimal.valueOf(0.5), Side.SELL));
        orderService.placeOrder(createPlaceOrderRequest(2, 99, BigDecimal.valueOf(0.9), Side.SELL));
        orderService.placeOrder(createPlaceOrderRequest(1, 300, BigDecimal.valueOf(0.8), Side.BUY)); // buy all
        mockMvc.perform(get("/order/book/BTC-USD"))
                .andExpect(jsonPath("$.bids").isArray())
                .andExpect(jsonPath("$.bids").isEmpty())
                .andExpect(jsonPath("$.asks").isArray())
                .andExpect(jsonPath("$.asks").isEmpty())
        ;
    }

    private PlaceOrderRequest createPlaceOrderRequest(int userId, double price, BigDecimal size, Side side) {
        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setUserId(userId);
        request.setSize(size);
        request.setPrice(price);
        request.setSymbol("BTC-USD");
        request.setSide(side.name());
        request.setType(OrderType.LIMIT.name());
        return request;
    }
}