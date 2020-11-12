package org.nvlp.tradeserver.model;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;


@Component
@ConfigurationProperties("markets")
@PropertySource("classpath:market.properties")
public class CoinMarket {

    // key: "BTC-USD"
    private Map<String, MarketQuote> marketQuoteMap;

    public void setBase(Map<String, Quotes> baseCurrencies) {
        marketQuoteMap = new HashMap<>();
        baseCurrencies.forEach((base, quotes) -> {
            quotes.quoteCurrencies.forEach((quote, marketQuote)->{
                marketQuoteMap.put(base+"-"+quote, marketQuote);
            });
        });
    }

    static class Quotes {
        // key: quote currency like USD
        private Map<String, MarketQuote> quoteCurrencies;

        public void setQuote(Map<String, MarketQuote> quoteCurrencies) {
            this.quoteCurrencies = quoteCurrencies;
        }
    }

    public boolean isValidPair(String pair) {
        return marketQuoteMap.containsKey(pair);
    }
    public MarketQuote getMarketQuote(String pair) {
        return marketQuoteMap.get(pair);
    }

    public void forEach(BiConsumer<String, MarketQuote> biConsumer) {
        marketQuoteMap.forEach(biConsumer);
    }
}

