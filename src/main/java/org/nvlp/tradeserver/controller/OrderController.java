package org.nvlp.tradeserver.controller;


import org.nvlp.tradeserver.model.PlaceOrderRequest;
import org.nvlp.tradeserver.model.ErrorCode;
import org.nvlp.tradeserver.model.OrderResponse;
import org.nvlp.tradeserver.service.OrderService;
import org.nvlp.tradeserver.service.OrderValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/order")
@RestController
public class OrderController {

    @Autowired
    private OrderValidationService orderValidationService;
    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity placeOrder(@RequestBody PlaceOrderRequest request) {
        if(!request.isValid()) {
            return ResponseEntity.badRequest().body("invalid place order request body");
        }
        ErrorCode errorCode = orderValidationService.validate(request);
        if(errorCode!=null) {
            return ResponseEntity.badRequest().body(errorCode);
        }

        OrderResponse orderResponse = orderService.placeOrder(request);
        return ResponseEntity.ok(orderResponse);
    }


}
