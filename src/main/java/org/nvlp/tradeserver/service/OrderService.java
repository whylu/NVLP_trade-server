package org.nvlp.tradeserver.service;


import org.nvlp.tradeserver.dto.PlaceOrderRequest;
import org.nvlp.tradeserver.model.OrderResponse;
import org.springframework.stereotype.Component;

@Component
public class OrderService {

    public OrderResponse placeOrder(PlaceOrderRequest request) {
        // take money from user wallet
        // put those money into system user 'market' wallet
        // insert order
        //   success:
        //     - return pending order
        //     - return partially transacted
        //     - return full transacted
        //   failed:
        //     - put money back to user wallet
        //     - return failed
        return null;
    }
}
