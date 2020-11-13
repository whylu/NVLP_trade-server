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

    //key: market, BTC-USD
    // in real case, we split this map into multiple queue to handle each market
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
        OrderInsertResult orderInsertResult = orderBook
                .insertOrder(request.getPrice(), request.getSize(), request.getSide());

        // TODO: audit balance for filledOrders

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


        // bids price order is in asc: 101, 100, 99, 98, ..
        // asks price order is in desc: 105, 106, 107, 108
        private ConcurrentNavigableMap<Double, PendingOrderQueue> bidBook = new ConcurrentSkipListMap<>(Comparator.reverseOrder());
        private ConcurrentNavigableMap<Double, PendingOrderQueue> askBook = new ConcurrentSkipListMap<>();
        // key: price, value: total size
        private ConcurrentNavigableMap<Double, BigDecimal> bidBookSummary = new ConcurrentSkipListMap<>(Comparator.reverseOrder());
        private ConcurrentNavigableMap<Double, BigDecimal> askBookSummary = new ConcurrentSkipListMap<>();


        // TODO: check race condition
        // this function is only for limit order
        public OrderInsertResult insertOrder(double price, BigDecimal size, Side side) {
            OrderInsertResult result = new OrderInsertResult();

            // match exist order
            List<FilledOrder> filledOrders = match(price, size, side);
            if(!filledOrders.isEmpty()) {
                BigDecimal filledSize = filledOrders.stream().map(fill -> fill.getSize()).reduce(BigDecimal.ZERO, BigDecimal::add);
                result.setFilledOrderList(filledOrders);
                result.setFilledSize(filledSize);
                size = size.subtract(filledSize);
            }

            // if order size remain, place order
            if(size.compareTo(BigDecimal.ZERO)>0) {
                PendingOrder pendingOrder = insertPendingOrder(price, size, side);
                result.addPending(pendingOrder);
            }

            return result;
        }


        private List<FilledOrder> match(double price, BigDecimal size, Side side) {
            ConcurrentNavigableMap<Double, PendingOrderQueue> book = side==Side.BUY? this.askBook : this.bidBook;
            ConcurrentNavigableMap<Double, BigDecimal> bookSummary = side==Side.BUY? this.askBookSummary : this.bidBookSummary;
            Comparator<Double> priceComparator = side==Side.BUY? Comparator.naturalOrder() : Comparator.reverseOrder();
            BigDecimal origSize = size;

            List<FilledOrder> filledOrders = Collections.emptyList();

            // stop condition: no order price match
            // is there any price match
            Map.Entry<Double, PendingOrderQueue> priceAndQueue;
            while (size.compareTo(BigDecimal.ZERO)>0
                    && (priceAndQueue=book.firstEntry())!=null
                    && priceComparator.compare(price, priceAndQueue.getKey())>=0) {
                PendingOrderQueue orderQueue = priceAndQueue.getValue();
                if(filledOrders.isEmpty()) {
                    filledOrders = new ArrayList<>();
                }
                while (size.compareTo(BigDecimal.ZERO)>0 && !orderQueue.isEmpty()) {
                    PendingOrder pendingOrder = orderQueue.firstEntry().getValue();

                    // size - pendingSize = remainSize, transactSize,  pendingRemain
                    //   1  -  1          =     0     ,    1        ,     0
                    //   2  -  1          =     1     ,    1        ,     0
                    //   1  -  3          =    -2     ,    1        ,     2
                    BigDecimal remainSize = size.subtract(pendingOrder.getSize());
                    if(remainSize.compareTo(BigDecimal.ZERO)>=0) {
                        // pending order fully transacted, remove from queue
                        FilledOrder filledOrder = pendingOrder.fullyTransact();

                        filledOrders.add(filledOrder);
                        orderQueue.pollFirstEntry(); // remove pendingOrder from queue
                        size = remainSize;
                    } else { // pending order partially transacted, reduce size
                        FilledOrder filledOrder = pendingOrder.transact(size);
                        filledOrders.add(filledOrder);
                        size = BigDecimal.ZERO;
                    }
                }
                if(orderQueue.isEmpty()) { // there is no pendingOrder in this price, remove price from book
                    book.remove(priceAndQueue.getKey());
                }
                addBookSummary(priceAndQueue.getKey(), size.subtract(origSize), bookSummary);
            }

            return filledOrders;
        }


        private PendingOrder insertPendingOrder(double price, BigDecimal size, Side side) {
            if(size.compareTo(BigDecimal.ZERO)<=0) {
                return null;
            }
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
            addBookSummary(price, size, bookSummary);
            //--- atomic operation ? ----

            return pendingOrder;
        }

        private void addBookSummary(double price, BigDecimal size, ConcurrentNavigableMap<Double, BigDecimal> bookSummary) {
            bookSummary.compute(price, (existPrice, existSize) -> {
                if(existSize==null) {
                    return size;
                }
                return existSize.add(size);
            });
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
