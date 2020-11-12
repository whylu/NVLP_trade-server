package org.nvlp.tradeserver.controller;

import org.nvlp.tradeserver.model.DepositRequest;
import org.nvlp.tradeserver.model.Wallet;
import org.nvlp.tradeserver.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/wallet")
@RestController
public class WalletController {

    @Autowired
    private WalletService walletService;

    @GetMapping("/{userId}/{currency}")
    public ResponseEntity getWallet(@PathVariable int userId, @PathVariable String currency) {
        Wallet wallet = walletService.getWallet(userId, currency);
        return ResponseEntity.ok(wallet);
    }

    @PostMapping("/deposit")
    public ResponseEntity deposit(@RequestBody DepositRequest depositRequest) {
        if(!depositRequest.isValid()) {
            return ResponseEntity.badRequest().body("invalid deposit request body");
        }
        Wallet wallet = walletService.deposit(depositRequest);
        return ResponseEntity.ok(wallet);
    }

}

