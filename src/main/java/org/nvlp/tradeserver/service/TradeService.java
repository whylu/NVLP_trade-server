package org.nvlp.tradeserver.service;

import org.nvlp.tradeserver.model.*;
import org.nvlp.tradeserver.model.enumn.Side;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class TradeService {

    private static final Logger LOG = LoggerFactory.getLogger(TradeService.class);

    private AtomicLong orderIdCounter = new AtomicLong(0);

    //key: market, BTC-USD
    // in real case, we split this map into multiple queue to handle each market
    private Map<String, OrderBook> marketOrderBooks = new HashMap<>();

    // key: orderId, value: userId
    private ConcurrentMap<Long, Integer> orderIdUserIdMap = new ConcurrentHashMap<>();

    @Autowired
    private CoinMarket coinMarket;

    @Autowired
    private WalletService walletService;

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
        realizeFillOrderAssetToWallet(request, orderInsertResult);
        updateOrderIdUserIdMap(request, orderInsertResult);
        return OrderResponse.of(request).compose(orderInsertResult);
    }

    /**
     *
     *         USD        ||        BTC
     *    total | frozen  ||   total | frozen
     *     1000 |    0    ||      0  |    0      deposit 1000 USD
     *      700 |  300    ||      0  |    0      place order  BUY BTC-USD price=1000, size=0.3
     *      700 |  100    ||    0.2  |    0      fill 0.2 BUY order -> cut off frozen 200USD, add 0.2BTC
     *
     *      700 |  100    ||   0.15  |  0.05     place order  SELL BTC-USD price=5000, size=0.05
     *      850 |  100    ||   0.15  |  0.02     fill 0.03 SELL order -> cut off frozen 0.03BTC, add 150USD
     *
     * @param orderInsertResult
     */
    private void realizeFillOrderAssetToWallet(PlaceOrderRequest request, OrderInsertResult orderInsertResult) {
        int takerId = request.getUserId();
        String base = request.getBase();
        String quote = request.getQuote();

        String takerCurrency = request.getSide()==Side.BUY? quote : base;
        String makerCurrency = request.getSide()==Side.BUY? base : quote;


        if(!orderInsertResult.getFilledOrderList().isEmpty()) {
            for (FilledOrder filledOrder : orderInsertResult.getFilledOrderList()) {
                int makerId = orderIdUserIdMap.get(filledOrder.getId());

                BigDecimal takerAmount = request.getSide()==Side.BUY? filledOrder.getCutOffFrozenAmount() : filledOrder.getRealizeAmount();
                BigDecimal makerAmount = request.getSide()==Side.BUY? filledOrder.getRealizeAmount() : filledOrder.getCutOffFrozenAmount();

                walletService.swapFrozenAndRealizeAmount(takerId, takerCurrency, takerAmount, makerId, makerCurrency, makerAmount);
            }
        }
    }

    private void updateOrderIdUserIdMap(PlaceOrderRequest request, OrderInsertResult orderInsertResult) {
        if(orderInsertResult.getPendingOrder()!=null) {
            orderIdUserIdMap.put(orderInsertResult.getPendingOrder().getId(), request.getUserId());
        }
        if(!orderInsertResult.getFilledOrderList().isEmpty()) {
            for (FilledOrder filledOrder : orderInsertResult.getFilledOrderList()) {
                if(filledOrder.isFilled()) {
                    orderIdUserIdMap.remove(filledOrder.getId());
                }
            }
        }
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
                BigDecimal filledSize = BigDecimal.ZERO;
                while (size.compareTo(BigDecimal.ZERO)>0 && !orderQueue.isEmpty()) {
                    PendingOrder pendingOrder = orderQueue.firstEntry().getValue();

                    // size - pendingSize = remainSize, transactSize,  pendingRemain
                    //   1  -  1          =     0     ,    1        ,     0
                    //   2  -  1          =     1     ,    1        ,     0
                    //   1  -  3          =    -2     ,    1        ,     2
                    BigDecimal remainSize = size.subtract(pendingOrder.getSize());
                    if(remainSize.compareTo(BigDecimal.ZERO)>=0) {
                        filledSize = filledSize.add(pendingOrder.getSize());
                        // pending order fully transacted, remove from queue
                        FilledOrder filledOrder = pendingOrder.fullyTransact();

                        filledOrders.add(filledOrder);
                        orderQueue.pollFirstEntry(); // remove pendingOrder from queue
                        size = remainSize;
                    } else { // pending order partially transacted, reduce size
                        filledSize = filledSize.add(size);
                        FilledOrder filledOrder = pendingOrder.transact(size);
                        filledOrders.add(filledOrder);
                        size = BigDecimal.ZERO;
                    }
                }
                if(orderQueue.isEmpty()) { // there is no pendingOrder in this price, remove price from book
                    book.remove(priceAndQueue.getKey());
                }
                updateBookSummary(priceAndQueue.getKey(), filledSize.negate(), bookSummary);
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
            updateBookSummary(price, size, bookSummary);
            //--- atomic operation ? ----

            return pendingOrder;
        }

        /**
         * add size into summary, size could be a negative value
         * if size become zero, remove that price row from summary
         * @param price
         * @param size
         * @param bookSummary
         */
        private void updateBookSummary(double price, BigDecimal size, ConcurrentNavigableMap<Double, BigDecimal> bookSummary) {
            bookSummary.compute(price, (existPrice, existSize) -> {
                if(existSize==null) {
                    return size;
                }
                BigDecimal remain = existSize.add(size);
                if(remain.compareTo(BigDecimal.ZERO)==0) {
                    return null; // which will remove this price
                } else {
                    return remain;
                }
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
