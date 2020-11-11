package org.nvlp.tradeserver.service;


import org.nvlp.tradeserver.dto.PlaceOrderRequest;
import org.nvlp.tradeserver.model.OrderResponse;
import org.nvlp.tradeserver.model.Wallet;
import org.nvlp.tradeserver.model.enumn.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderService {

    @Autowired
    private WalletService walletService;

    public OrderResponse placeOrder(PlaceOrderRequest request) {

        Wallet frozen = walletService.freeze(request.freezeCurrency(), request.freezeAmount(), request.getUserId());
        if(frozen!=null) {

            // insert order
            //   success:
            //     - return pending order
            //     - return partially transacted
            //     - return full transacted
            //   failed:
            //     - put money back to user wallet
            //     - return failed

            return null;
        } else { // insufficient balance
            return OrderResponse.of(request).setStatus(OrderStatus.REJECTED);
        }
    }
}
