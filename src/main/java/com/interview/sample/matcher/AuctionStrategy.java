package com.interview.sample.matcher;

import com.interview.sample.books.OrderBook;
import com.interview.sample.domain.event.Event;
import com.interview.sample.domain.event.TradeEvent;
import com.interview.sample.domain.order.Order;

import java.util.List;

public interface AuctionStrategy {
    List<Event> match(OrderBook book, Order order);
}