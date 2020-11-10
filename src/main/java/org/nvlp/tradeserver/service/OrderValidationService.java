package org.nvlp.tradeserver.service;

import org.nvlp.tradeserver.dto.PlaceOrderRequest;
import org.nvlp.tradeserver.model.CoinMarket;
import org.nvlp.tradeserver.model.MarketQuote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderValidationService {


    @Autowired
    private CoinMarket coinMarket;


    /**
     * if request invalid, return error message
     * @param request
     * @return
     */
    public String validate(PlaceOrderRequest request) {
        if(!coinMarket.isValidPair(request.getSymbol())) {
            return "invalid market";
        }
        MarketQuote marketQuote = coinMarket.getMarketQuote(request.getSymbol());

        // valid order
//        marketQuote.getMinPrice()

        // valid wallet

        return null;
    }
}
