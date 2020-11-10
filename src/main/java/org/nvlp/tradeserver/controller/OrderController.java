package org.nvlp.tradeserver.controller;


import org.nvlp.tradeserver.dto.PlaceOrderRequest;
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

    @PostMapping
    public ResponseEntity placeOrder(@RequestBody PlaceOrderRequest request) {
        if(!request.isValid()) {
            return ResponseEntity.badRequest().body("invalid place order request body");
        }
        String invalidMessage = orderValidationService.validate(request);
        if(invalidMessage!=null) {
            return ResponseEntity.badRequest().body(invalidMessage);
        }



        return null;
    }


}
