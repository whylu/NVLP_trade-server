package org.nvlp.tradeserver.service;


import org.springframework.stereotype.Component;

@Component
public class OrderService {

    public void placeOrder() {
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
    }
}
