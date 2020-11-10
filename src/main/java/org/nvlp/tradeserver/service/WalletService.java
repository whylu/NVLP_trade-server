package org.nvlp.tradeserver.service;

import org.nvlp.tradeserver.dto.DepositRequest;
import org.nvlp.tradeserver.model.Wallet;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class WalletService {

    private ConcurrentMap<String, Wallet> walletRepository = new ConcurrentHashMap<>();

    public Wallet getWallet(int userId, String currency) {
        // simulate query from DB
        return walletRepository.getOrDefault(formatKey(userId, currency), new Wallet(userId, currency));
    }


    // thread-safe
    public Wallet deposit(DepositRequest depositRequest) {
        String key = formatKey(depositRequest.getUserId(), depositRequest.getCurrency());

        // simulate update DB:  amount += depositRequest.getAmount()
        // here I use ConcurrentHashMap to solve race-condition
        // in real case, should consider on DB level
        return walletRepository.compute(key, (existedKey, wallet) ->{ //merge for initail
            if(wallet==null) { // not existed, initial
                wallet = new Wallet(depositRequest.getUserId(), depositRequest.getCurrency());
            }
            wallet.addAmount(depositRequest.getAmount());
            return wallet;
        });
    }


    // I just want to try non-thread-safe issue
//    public Wallet deposit(DepositRequest depositRequest) {
//        String key = formatKey(depositRequest.getUserId(), depositRequest.getCurrency());
//        Wallet wallet = getWallet(depositRequest.getUserId(), depositRequest.getCurrency());
//        wallet.addAmount(depositRequest.getAmount());
//
//        // simulate update DB:  amount += depositRequest.getAmount()
//        // here I use ConcurrentHashMap to solve race-condition
//        // in real case, should consider on DB level
//        walletRepository.put(key, wallet);
//        return wallet;
//    }

    private String formatKey(int userId, String currency) {
        return userId+currency;
    }
}
