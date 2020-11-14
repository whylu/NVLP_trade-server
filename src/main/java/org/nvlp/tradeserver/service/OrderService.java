package org.nvlp.tradeserver.service;


import org.nvlp.tradeserver.model.ErrorCode;
import org.nvlp.tradeserver.model.OrderResponse;
import org.nvlp.tradeserver.model.PlaceOrderRequest;
import org.nvlp.tradeserver.model.Wallet;
import org.nvlp.tradeserver.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderService {
    private static final Logger LOG = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private WalletService walletService;

    @Autowired
    private TradeService tradeService;

    public OrderResponse placeOrder(PlaceOrderRequest request) {

        Wallet frozen = walletService.freeze(request.getFreezeCurrency(), request.getFreezeAmount(), request.getUserId());
        if(frozen!=null) {
            OrderResponse response = tradeService.place(request);
            if (response.isRejected()) {
                Wallet unfreeze = walletService.unfreeze(request.getFreezeCurrency(), request.getFreezeAmount(), request.getUserId());
                if(unfreeze==null) {
                    LOG.error("[unfreeze failed] wallet:{}, request:{}", JsonUtils.toString(frozen), JsonUtils.toString(request));
                }
            }
            return response;
        } else { // insufficient balance
            return OrderResponse.of(request).reject(ErrorCode.INSUFFICIENT_BALANCE);
        }
    }
}
