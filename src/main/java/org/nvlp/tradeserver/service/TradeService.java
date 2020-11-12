package org.nvlp.tradeserver.service;

import org.nvlp.tradeserver.model.*;
import org.nvlp.tradeserver.model.enumn.Side;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class TradeService {

    private AtomicLong orderIdCounter = new AtomicLong(0);

    private Map<String, OrderBook> marketOrderBooks = new HashMap<>();

    @Autowired
    private CoinMarket coinMarket;

    @PostConstruct
    private void init() {
        coinMarket.forEach((marketPair, marketQuote)->{
            marketOrderBooks.put(marketPair, new OrderBook());
        });
    }

    private long nextOrderId() {
        return orderIdCounter.incrementAndGet();
    }


    public OrderResponse place(PlaceOrderRequest request) {
        OrderBook orderBook = marketOrderBooks.get(request.getSymbol());
        Side side = request.getSide();
        OrderInsertResult orderInsertResult = switch (side) {
            case BUY -> orderBook.insertBid(request.getPrice(), request.getSize());
            case SELL -> null;
        };

        // insert order
        //   success:
        //     - return pending order
        //     - return partially transacted
        //     - return full transacted

        return OrderResponse.of(request).compose(orderInsertResult);
    }

    public Map<Double, BigDecimal> getBids(String symbol) {
        OrderBook orderBook = marketOrderBooks.get(symbol);
        if(orderBook==null) {
            return Collections.emptyMap();
        }
        return orderBook.getBidBookSummary();
    }


    private class OrderBook {

        private ConcurrentNavigableMap<Double, PendingOrderQueue> bidBook = new ConcurrentSkipListMap<>();
        // key: price, value: total size
        private ConcurrentNavigableMap<Double, BigDecimal> bidBookSummary = new ConcurrentSkipListMap<>();


        // TODO: check race condition
        public OrderInsertResult insertBid(double price, double size) {
            OrderInsertResult result = new OrderInsertResult();

            // match exist order TODO:
            // filledOrder


            // if order size remain, place order
            PendingOrderQueue pendingOrderQueue = bidBook.get(price);
            if(pendingOrderQueue==null) { // init
                pendingOrderQueue = new PendingOrderQueue();
                bidBook.put(price, pendingOrderQueue);
            }
            long orderId = nextOrderId();
            PendingOrder pendingOrder = new PendingOrder(orderId, price, BigDecimal.valueOf(size));

            //--- atomic operation ? ----
            pendingOrderQueue.put(pendingOrder.getId(), pendingOrder);
            bidBookSummary.compute(price, (existPrice, existSize) -> {
                if(existSize==null) {
                    return BigDecimal.valueOf(size);
                }
                return existSize.add(BigDecimal.valueOf(size));
            });
            //--- atomic operation ? ----

            result.addPending(pendingOrder);
            return result;
        }

        public ConcurrentNavigableMap<Double, BigDecimal> getBidBookSummary() {
            return bidBookSummary;
        }


        /**
         * key: orderId
         */
        private class PendingOrderQueue extends ConcurrentSkipListMap<Long, PendingOrder>{
        }

    }

}
