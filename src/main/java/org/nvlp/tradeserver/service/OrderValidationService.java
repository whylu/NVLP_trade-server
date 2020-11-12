package org.nvlp.tradeserver.service;

import org.nvlp.tradeserver.model.PlaceOrderRequest;
import org.nvlp.tradeserver.model.CoinMarket;
import org.nvlp.tradeserver.model.ErrorCode;
import org.nvlp.tradeserver.model.MarketQuote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class OrderValidationService {


    @Autowired
    private CoinMarket coinMarket;


    /**
     * if request invalid, return error message
     * @param request
     * @return
     */
    public ErrorCode validate(PlaceOrderRequest request) {
        if(!coinMarket.isValidPair(request.getSymbol())) {
            return ErrorCode.INVALID_MARKER;
        }
        MarketQuote marketQuote = coinMarket.getMarketQuote(request.getSymbol());


        BigDecimal remainPrice = BigDecimal.valueOf(request.getPrice()).subtract(marketQuote.getMinPrice());
        if(remainPrice.compareTo(BigDecimal.ZERO)<0)
            return ErrorCode.INVALID_ORDER_PRICE_MIN;

        if(remainPrice.remainder(marketQuote.getMinPriceIncrement()).compareTo(BigDecimal.ZERO)>0)
            return ErrorCode.INVALID_ORDER_PRICE_INCREMENT;

        BigDecimal remainSize = request.getSize().subtract(marketQuote.getMinSize());
        if(remainSize.compareTo(BigDecimal.ZERO)<0)
            return ErrorCode.INVALID_ORDER_SIZE_MIN;
        if(remainSize.remainder(marketQuote.getMinSizeIncrement()).compareTo(BigDecimal.ZERO)>0)
            return ErrorCode.INVALID_ORDER_SIZE_INCREMENT;

        return null; // pass
    }
}
