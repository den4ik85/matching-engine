package com.interview.sample.matcher;

import com.interview.sample.books.OrderBook;
import com.interview.sample.books.PriceLevel;
import com.interview.sample.domain.event.Event;
import com.interview.sample.domain.event.OrderCancelledEvent;
import com.interview.sample.domain.event.OrderRejectedEvent;
import com.interview.sample.domain.event.TradeEvent;
import com.interview.sample.domain.order.*;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class PriceTimeMatcher implements AuctionStrategy {
    @Override
    public List<Event> match(OrderBook book, Order aggressorOrder) {
        List<Event> events = new LinkedList<>();

        // Specific validation should be added here in scope of separate service
        if (!isValidTimeInForce(aggressorOrder)) {
            aggressorOrder.setStatus(OrderStatus.REJECTED);
            events.add(new OrderRejectedEvent(
                    aggressorOrder.getOrderId(),
                    aggressorOrder.getClientId(),
                    "Invalid TimeInForce for OrderType"
            ));
            return events;
        }

        if (aggressorOrder.getSide() == OrderSide.BUY) {
            matchAgainst(book.getBookBySide(OrderSide.SELL), aggressorOrder, events);
        } else {
            matchAgainst(book.getBookBySide(OrderSide.BUY), aggressorOrder, events);
        }

        updateOrderStatus(aggressorOrder, events);
        return events;
    }

    private boolean isValidTimeInForce(Order order) {
        // Other combinations can be added as needed
        return (order.getType() == OrderType.LIMIT && order.getTimeInForce() == TimeInForce.ALL_OR_NONE) ||
                (order.getType() == OrderType.MARKET && order.getTimeInForce() == TimeInForce.FILL_OR_KILL);
    }

    private void matchAgainst(NavigableMap<Price, PriceLevel> opposingBook,
                              Order aggressorOrder, List<Event> events) {
        OrderQuantity quantity = aggressorOrder.getQuantity();

        while (!quantity.isFullyFilled() && !opposingBook.isEmpty()) {
            Map.Entry<Price, PriceLevel> bestLevel = opposingBook.firstEntry();
            Price bestPrice = bestLevel.getKey();

            if (aggressorOrder.getType() == OrderType.LIMIT) {
                boolean priceConditionMet = aggressorOrder.getSide() == OrderSide.BUY
                        ? aggressorOrder.getPrice().compareTo(bestPrice) >= 0
                        : aggressorOrder.getPrice().compareTo(bestPrice) <= 0;
                if (!priceConditionMet) break;
            }

            PriceLevel level = bestLevel.getValue();
            Order restingOrder = level.peek();
            OrderQuantity restingQuantity = restingOrder.getQuantity();

            int tradeQty = calculateTradeQuantity(quantity, restingQuantity, aggressorOrder);
            if (tradeQty <= 0) break;

            executeTrade(aggressorOrder, restingOrder, bestPrice, tradeQty, events);
            updateQuantities(quantity, restingQuantity, tradeQty);
            cleanupFilledOrders(level, restingOrder, opposingBook, bestPrice);
        }
    }

    private int calculateTradeQuantity(OrderQuantity aggressorQty,
                                       OrderQuantity restingQty,
                                       Order order) {
        int maxPossible = Math.min(
                aggressorQty.getRemainingQuantity(),
                restingQty.getRemainingQuantity()
        );

        if (order.getTimeInForce() == TimeInForce.ALL_OR_NONE &&
                maxPossible < aggressorQty.getRemainingQuantity()) {
            return 0;
        }

        return maxPossible;
    }

    private void executeTrade(Order aggressorOrder, Order restingOrder,
                              Price tradePrice, int tradeQty, List<Event> events) {
        events.add(new TradeEvent(
                aggressorOrder.getOrderId(),
                restingOrder.getOrderId(),
                aggressorOrder.getInstrumentId(),
                tradePrice,
                tradeQty,
                System.nanoTime()
        ));
    }

    private void updateQuantities(OrderQuantity aggressorQty,
                                  OrderQuantity restingQty,
                                  int tradeQty) {
        aggressorQty.setRemainingQuantity(
                aggressorQty.getRemainingQuantity() - tradeQty
        );
        restingQty.setRemainingQuantity(
                restingQty.getRemainingQuantity() - tradeQty
        );
    }

    private void cleanupFilledOrders(PriceLevel level, Order restingOrder,
                                     NavigableMap<Price, PriceLevel> opposingBook,
                                     Price bestPrice) {
        if (restingOrder.getQuantity().isFullyFilled()) {
            level.getOrders().poll();
            if (level.isEmpty()) {
                opposingBook.remove(bestPrice);
            }
        }
    }

    private void updateOrderStatus(Order order, List<Event> events) {
        OrderQuantity qty = order.getQuantity();
        if (qty.isFullyFilled()) {
            order.setStatus(OrderStatus.FILLED);
        } else if (qty.isPartiallyFilled()) {
            order.setStatus(OrderStatus.PARTIALLY_FILLED);
        } else if (order.getType() == OrderType.MARKET ||
                order.getTimeInForce() == TimeInForce.FILL_OR_KILL) {
            // Cancel only MARKET orders or FILL_OR_KILL orders if not fully filled
            order.setStatus(OrderStatus.REJECTED);
            events.add(new OrderRejectedEvent(
                    order.getOrderId(),
                    order.getClientId(),
                    "Order could not be fully filled"
            ));
        }
    }
}