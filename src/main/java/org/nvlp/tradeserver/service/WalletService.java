package org.nvlp.tradeserver.service;

import org.nvlp.tradeserver.model.DepositRequest;
import org.nvlp.tradeserver.model.Wallet;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
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
        return increaseAmount(depositRequest.getUserId(), depositRequest.getCurrency(), depositRequest.getAmount());
    }

    private Wallet increaseAmount(int userId, String currency, double amount) {
        return increaseAmount(userId, currency, BigDecimal.valueOf(amount));
    }
    private Wallet increaseAmount(int userId, String currency, BigDecimal amount) {
        String key = formatKey(userId, currency);

        // simulate update DB:  amount += depositRequest.getAmount()
        // here I use ConcurrentHashMap to solve race-condition
        // in real case, should consider on DB level
        return walletRepository.compute(key, (existedKey, wallet) ->{
            if(wallet==null) { // not existed, initial
                wallet = new Wallet(userId, currency);
            }
            wallet.addAmount(amount);
            return wallet;
        });
    }

    /**
     * null for failed, which means insufficient balance, or return result wallet if success
     * @param userId
     * @param currency
     * @param amount
     * @return
     */
    private Wallet decreaseAmount(int userId, String currency, BigDecimal amount) {
        String key = formatKey(userId, currency);

        return walletRepository.compute(key, (existedKey, wallet) ->{
            if(wallet==null) { // not existed, insufficient balance
                return null;
            }
            BigDecimal result = wallet.decreaseAmount(amount);
            if(result==null) { // failed
                return null;
            }
            return wallet;
        });
    }
    public boolean transfer(String currency, BigDecimal amount, int fromUserId, int toUserId) {
        Wallet wallet = decreaseAmount(fromUserId, currency, amount);
        if(wallet!=null) {
            increaseAmount(toUserId, currency, amount);
            return true;
        }
        return false;
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

    public Wallet freeze(String currency, BigDecimal amount, int userId) {
        String key = formatKey(userId, currency);

        return walletRepository.compute(key, (existedKey, wallet) ->{
            if(wallet==null) { // not existed, insufficient balance
                return null;
            }
            BigDecimal result = wallet.freeze(amount);
            if(result==null) { // failed
                return null;
            }
            return wallet;
        });

    }
}
