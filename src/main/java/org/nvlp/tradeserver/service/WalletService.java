package org.nvlp.tradeserver.service;

import org.nvlp.tradeserver.model.DepositRequest;
import org.nvlp.tradeserver.model.Wallet;
import org.nvlp.tradeserver.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class WalletService {

    private static Logger LOG = LoggerFactory.getLogger(WalletService.class);

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
    public Wallet increaseAmount(int userId, String currency, BigDecimal amount) {
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

    public Wallet unfreeze(String currency, BigDecimal amount, int userId) {
        String key = formatKey(userId, currency);

        return walletRepository.compute(key, (existedKey, wallet) ->{
            if(wallet==null) { // not existed, insufficient balance
                return null;
            }
            BigDecimal result = wallet.unfreeze(amount);
            if(result==null) { // failed
                return null;
            }
            return wallet;
        });
    }

    /**
     * this is done by a SQL in real case
     *
     *  taker frozen -> maker amount
     *  maker frozen -> taker amount
     *
     *  takerPayWallet --(takerAmount of takerCurrency)--> makerReceiveWallet
     *  makerPayWallet --(makerAmount of makerCurrency)--> takerReceiveWallet
     *  @param takerId
     * @param takerCurrency
     * @param takerAmount
     * @param makerId
     * @param makerCurrency
     * @param makerAmount
     * @return
     */
    public boolean swapFrozenAndRealizeAmount(int takerId, String takerCurrency, BigDecimal takerAmount, int makerId, String makerCurrency, BigDecimal makerAmount) {
        Wallet takerPayWallet = null;
        Wallet takerReceiveWallet = null;
        Wallet makerPayWallet = null;
        Wallet makerReceiveWallet = null;
        synchronized (walletRepository) {
            takerPayWallet = getOrCreateWallet(takerId, takerCurrency);
            takerReceiveWallet = getOrCreateWallet(takerId, makerCurrency);
            makerPayWallet = getOrCreateWallet(makerId, makerCurrency);
            makerReceiveWallet = getOrCreateWallet(makerId, takerCurrency);
        }

        boolean success = payFrozenAndReceiveRealize(takerPayWallet, makerReceiveWallet, takerAmount);
        if(success) {
            success = payFrozenAndReceiveRealize(makerPayWallet, takerReceiveWallet, makerAmount);
            if(success) {
                LOG.info("swapFrozenAndRealizeAmount success: {}--({} {})-->{}, {}--({} {})-->{}",
                        takerId, takerAmount, takerCurrency, makerId, makerId, makerAmount, makerCurrency, takerId);

            } else {
                LOG.info("payFrozenAndReceiveRealize failed, makerAmount:{}, makerPayWallet:{}, takerReceiveWallet:{}",
                        makerAmount, JsonUtils.toString(makerPayWallet), JsonUtils.toString(takerReceiveWallet));
                rollbackPayFrozenAndReceiveRealize(takerPayWallet, makerReceiveWallet, takerAmount);
            }
        } else {
            LOG.info("payFrozenAndReceiveRealize failed, takerAmount:{}, takerPayWallet:{}, makerReceiveWallet:{}",
                    makerAmount, JsonUtils.toString(makerPayWallet), JsonUtils.toString(takerReceiveWallet));
        }
        return success;
    }

    private void rollbackPayFrozenAndReceiveRealize(Wallet payWallet, Wallet receiveWallet, BigDecimal amount) {
        BigDecimal decreased = receiveWallet.decreaseAmount(amount);
        if(decreased!=null) {
            payWallet.addAmount(amount);
            payWallet.freeze(amount);
        }
//        LOG
    }

    private boolean payFrozenAndReceiveRealize(Wallet payWallet, Wallet receiveWallet, BigDecimal amount) {
        BigDecimal cut = payWallet.cutOffFrozen(amount);
        if(cut!=null) {
             receiveWallet.addAmount(amount);
             return true;
        }
        return false;
    }

    private Wallet getOrCreateWallet(int userId, String currency) {
        String key = formatKey(userId, currency);
        Wallet wallet = walletRepository.get(key);
        if(wallet==null) {
            wallet = new Wallet(userId, currency);
            walletRepository.put(key, wallet);
        }
        return wallet;
    }

}
