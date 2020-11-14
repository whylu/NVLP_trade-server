package org.nvlp.tradeserver.model;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;


@Import(CoinMarket.class)

@EnableConfigurationProperties
@RunWith(SpringRunner.class)
public class CoinMarketTest {

    @Autowired
    private CoinMarket coinMarket;

    @Test
    public void isValidPair() {
        assertThat(coinMarket.isValidPair("MING-USD")).isTrue();
        assertThat(coinMarket.isValidPair("MING-JPY")).isTrue();
        assertThat(coinMarket.isValidPair("XRP-USD")).isTrue();
        assertThat(coinMarket.isValidPair("NNNNN-USD")).isFalse();
    }

    @Test
    public void getMarketQuote() {
        MarketQuote marketQuote = coinMarket.getMarketQuote("MING-USD");
        assertThat(marketQuote.getMinPrice()).isEqualByComparingTo(BigDecimal.valueOf(0.5));
        assertThat(marketQuote.getMinPriceIncrement()).isEqualByComparingTo(BigDecimal.valueOf(0.5));
        assertThat(marketQuote.getMinSize()).isEqualByComparingTo(BigDecimal.valueOf(0.0002));
        assertThat(marketQuote.getMinSizeIncrement()).isEqualByComparingTo(BigDecimal.valueOf(0.0001));

        marketQuote = coinMarket.getMarketQuote("MING-JPY");
        assertThat(marketQuote.getMinPrice()).isEqualByComparingTo(BigDecimal.valueOf(5));

        marketQuote = coinMarket.getMarketQuote("XRP-USD");
        assertThat(marketQuote.getMinPrice()).isEqualByComparingTo(BigDecimal.valueOf(0.05));
    }
}