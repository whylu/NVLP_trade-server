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
        OrderInsertResult orderInsertResult = orderBook
                .insertOrder(request.getPrice(), request.getSize(), request.getSide());

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
    public Map<Double, BigDecimal> getAsks(String symbol) {
        OrderBook orderBook = marketOrderBooks.get(symbol);
        if(orderBook==null) {
            return Collections.emptyMap();
        }
        return orderBook.getAskBookSummary();
    }


    private class OrderBook {

        private ConcurrentNavigableMap<Double, PendingOrderQueue> bidBook = new ConcurrentSkipListMap<>();
        private ConcurrentNavigableMap<Double, PendingOrderQueue> askBook = new ConcurrentSkipListMap<>();
        // key: price, value: total size
        private ConcurrentNavigableMap<Double, BigDecimal> bidBookSummary = new ConcurrentSkipListMap<>();
        private ConcurrentNavigableMap<Double, BigDecimal> askBookSummary = new ConcurrentSkipListMap<>();


        // TODO: check race condition
        public OrderInsertResult insertOrder(double price, BigDecimal size, Side side) {
            OrderInsertResult result = new OrderInsertResult();

            // match exist order TODO:
            // filledOrder

            // if order size remain, place order
            PendingOrder pendingOrder = insertPendingOrder(price, size, side);
            result.addPending(pendingOrder);

            return result;
        }


        private PendingOrder insertPendingOrder(double price, BigDecimal size, Side side) {
            ConcurrentNavigableMap<Double, PendingOrderQueue> book = side==Side.BUY? this.bidBook : this.askBook;
            ConcurrentNavigableMap<Double, BigDecimal> bookSummary = side==Side.BUY? this.bidBookSummary : this.askBookSummary;

            PendingOrderQueue pendingOrderQueue = book.get(price);
            if(pendingOrderQueue==null) { // init
                pendingOrderQueue = new PendingOrderQueue();
                book.put(price, pendingOrderQueue);
            }
            long orderId = nextOrderId();
            PendingOrder pendingOrder = new PendingOrder(orderId, price, size, side);

            //--- atomic operation ? ----
            pendingOrderQueue.put(pendingOrder.getId(), pendingOrder);
            bookSummary.compute(price, (existPrice, existSize) -> {
                if(existSize==null) {
                    return size;
                }
                return existSize.add(size);
            });
            //--- atomic operation ? ----

            return pendingOrder;
        }


        public ConcurrentNavigableMap<Double, BigDecimal> getBidBookSummary() {
            return bidBookSummary;
        }

        public ConcurrentNavigableMap<Double, BigDecimal> getAskBookSummary() {
            return askBookSummary;
        }

        /**
         * key: orderId
         */
        private class PendingOrderQueue extends ConcurrentSkipListMap<Long, PendingOrder>{
        }

    }
}
