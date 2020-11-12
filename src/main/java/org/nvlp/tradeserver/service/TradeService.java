package org.nvlp.tradeserver.service;

import org.nvlp.tradeserver.model.OrderResponse;
import org.nvlp.tradeserver.model.PlaceOrderRequest;
import org.springframework.stereotype.Component;

@Component
public class TradeService {
    public OrderResponse place(PlaceOrderRequest request) {

        // insert order
        //   success:
        //     - return pending order
        //     - return partially transacted
        //     - return full transacted
        //   failed:
        //     - put money back to user wallet
        //     - return failed

        return OrderResponse.of(request).reject();
    }
}
