package org.nvlp.tradeserver.controller;


import org.nvlp.tradeserver.model.ErrorCode;
import org.nvlp.tradeserver.model.OrderResponse;
import org.nvlp.tradeserver.model.PlaceOrderRequest;
import org.nvlp.tradeserver.model.dto.OrderBook;
import org.nvlp.tradeserver.service.OrderService;
import org.nvlp.tradeserver.service.OrderValidationService;
import org.nvlp.tradeserver.service.TradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RequestMapping("/order")
@RestController
public class OrderController {

    @Autowired
    private OrderValidationService orderValidationService;
    @Autowired
    private OrderService orderService;

    @Autowired
    private TradeService tradeService;

    @PostMapping
    public ResponseEntity placeOrder(@RequestBody PlaceOrderRequest request) {
        ErrorCode errorCode = request.isValid();
        if(errorCode!=null) {
            return ResponseEntity.ok(OrderResponse.of(request).reject(errorCode));
        }
        errorCode = orderValidationService.validate(request);
        if(errorCode!=null) {
            return ResponseEntity.ok(OrderResponse.of(request).reject(errorCode));
        }

        OrderResponse orderResponse = orderService.placeOrder(request);
        return ResponseEntity.ok(orderResponse);
    }

    @GetMapping("/book/{symbol}")
    public ResponseEntity book(@PathVariable String symbol) {
        Map<Double, BigDecimal> asks = tradeService.getAsks(symbol);
        Map<Double, BigDecimal> bids = tradeService.getBids(symbol);
        return ResponseEntity.ok(new OrderBook(bids, asks));

    }


}
