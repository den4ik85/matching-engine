package com.interview.sample.books;

import com.interview.sample.domain.event.Event;
import com.interview.sample.domain.event.OrderCancelledEvent;
import com.interview.sample.domain.instrument.Instrument;
import com.interview.sample.domain.order.Order;
import com.interview.sample.domain.order.OrderSide;
import com.interview.sample.domain.order.OrderType;
import com.interview.sample.domain.order.Price;
import com.interview.sample.matcher.AuctionStrategy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.*;


@AllArgsConstructor
@Builder
public class OrderBook {
    // Price levels for bids (descending) and asks (ascending)
    private final NavigableMap<Price, PriceLevel> bids = new TreeMap<>(Comparator.reverseOrder());
    private final NavigableMap<Price, PriceLevel> asks = new TreeMap<>();

    // Active orders by ID
    private final Map<String, Order> orders = new HashMap<>();
    private final AuctionStrategy matcher;
    @Getter
    private final Instrument instrument;

    // Cache top-of-book prices
    @Getter
    private volatile Price bestBidPrice = null;
    @Getter
    private volatile Price bestAskPrice = null;

    public List<Event> placeOrder(Order order) {
        orders.put(order.getClientOrderId(), order);

        if (order.getType() == OrderType.LIMIT) {
            getBookBySide(order.getSide())
                    .computeIfAbsent(order.getPrice(), p -> new PriceLevel())
                    .addOrder(order);

            updateTopOfBook(order.getSide(), order.getPrice());
        }

        return matcher.match(this, order);
    }

    public Optional<OrderCancelledEvent> cancelOrder(String orderId) {
        Order order = orders.remove(orderId);
        if (order == null) return Optional.empty();

        if (order.getType() == OrderType.LIMIT) {
            NavigableMap<Price, PriceLevel> book = getBookBySide(order.getSide());
            Price price = order.getPrice();
            PriceLevel level = book.get(price);

            if (level != null) {
                level.removeOrder(order);
                if (level.isEmpty()) {
                    book.remove(price);
                    checkTopOfBookAfterRemoval(order.getSide(), price);
                }
            }
        }

        return Optional.of(new OrderCancelledEvent(order.getClientOrderId(), order.getClientId(), order.getInstrumentId()));
    }

    public Optional<Price> getMarketPrice() {
        if (bestBidPrice != null && bestAskPrice != null) {
            long midValue = (bestBidPrice.getValue() + bestAskPrice.getValue()) / 2;
            return Optional.of(new Price(midValue, bestBidPrice.getScale()));
        }
        return Optional.empty();
    }

    private void updateTopOfBook(OrderSide side, Price price) {
        if (side == OrderSide.BUY) {
            if (bestBidPrice == null || price.compareTo(bestBidPrice) > 0) {
                bestBidPrice = price;
            }
        } else {
            if (bestAskPrice == null || price.compareTo(bestAskPrice) < 0) {
                bestAskPrice = price;
            }
        }
    }

    private void checkTopOfBookAfterRemoval(OrderSide side, Price removedPrice) {
        if (side == OrderSide.BUY) {
            if (bestBidPrice != null && bestBidPrice.equals(removedPrice)) {
                bestBidPrice = bids.isEmpty() ? null : bids.firstKey();
            }
        } else {
            if (bestAskPrice != null && bestAskPrice.equals(removedPrice)) {
                bestAskPrice = asks.isEmpty() ? null : asks.firstKey();
            }
        }
    }

    public NavigableMap<Price, PriceLevel> getBookBySide(OrderSide side) {
        return side == OrderSide.BUY ? bids : asks;
    }
}