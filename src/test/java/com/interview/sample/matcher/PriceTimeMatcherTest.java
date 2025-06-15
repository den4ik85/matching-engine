package com.interview.sample.matcher;


import com.interview.sample.books.OrderBook;
import com.interview.sample.books.PriceLevel;
import com.interview.sample.domain.event.Event;
import com.interview.sample.domain.event.TradeEvent;
import com.interview.sample.domain.order.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PriceTimeMatcherTest {

    @Mock
    private OrderBook orderBook;

    @InjectMocks
    private PriceTimeMatcher matcher;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testMatchGeneratesTradeEvent() {
        // Arrange
        Order buyOrder = new Order("instrument1", "client1", "order1", OrderSide.BUY, OrderType.LIMIT, new Price(100, 2), OrderQuantity.builder().originalQuantity(10).remainingQuantity(10).build(), TimeInForce.ALL_OR_NONE);
        Order sellOrder = new Order("instrument1", "client2", "order2", OrderSide.SELL, OrderType.MARKET, new Price(100, 2), OrderQuantity.builder().originalQuantity(10).remainingQuantity(10).build(), TimeInForce.FILL_OR_KILL);

        PriceLevel buyLevel = mock(PriceLevel.class);
        PriceLevel sellLevel = mock(PriceLevel.class);
        when(sellLevel.peek()).thenReturn(sellOrder);
        when(buyLevel.peek()).thenReturn(buyOrder);
        when(buyLevel.peek()).thenReturn(buyOrder);
        when(sellLevel.getOrders()).thenReturn(mock(Queue.class));
        when(buyLevel.getOrders()).thenReturn(mock(Queue.class));
        when(orderBook.getBookBySide(OrderSide.BUY)).thenReturn(new TreeMap<>(Map.of(buyOrder.getPrice(), buyLevel)));
        when(orderBook.getBookBySide(OrderSide.SELL)).thenReturn(new TreeMap<>(Map.of(sellOrder.getPrice(), sellLevel)));
        when(buyLevel.peek()).thenReturn(buyOrder);
        when(sellLevel.peek()).thenReturn(sellOrder);

        // Act
        List<Event> events = matcher.match(orderBook, buyOrder);

        // Assert
        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof TradeEvent);
        TradeEvent tradeEvent = (TradeEvent) events.get(0);
        assertEquals(10, tradeEvent.getQuantity());
        assertEquals(new Price(100, 2), tradeEvent.getPrice());
    }

    @Test
    void testNoMatchWhenPricesDoNotMatch() {
        // Arrange
        Order buyOrder = new Order("instrument1", "client1", "order1", OrderSide.BUY, OrderType.LIMIT, new Price(100, 2), OrderQuantity.builder().originalQuantity(10).remainingQuantity(10).build(), TimeInForce.ALL_OR_NONE);
        Order sellOrder = new Order("instrument1", "client2", "order2", OrderSide.SELL, OrderType.MARKET, new Price(101, 2), OrderQuantity.builder().originalQuantity(10).remainingQuantity(10).build(), TimeInForce.FILL_OR_KILL);

        PriceLevel buyLevel = mock(PriceLevel.class);
        PriceLevel sellLevel = mock(PriceLevel.class);

        when(orderBook.getBookBySide(OrderSide.BUY)).thenReturn(new TreeMap<>(Map.of(buyOrder.getPrice(), buyLevel)));
        when(orderBook.getBookBySide(OrderSide.SELL)).thenReturn(new TreeMap<>(Map.of(sellOrder.getPrice(), sellLevel)));
        when(buyLevel.peek()).thenReturn(buyOrder);
        when(sellLevel.peek()).thenReturn(sellOrder);

        // Act
        List<Event> events = matcher.match(orderBook, buyOrder);

        // Assert
        assertTrue(events.isEmpty());
    }

    @Test
    void testNoMatchWhenOrderBookIsEmpty() {
        // Arrange
        Order buyOrder = new Order("instrument1", "client1", "order1", OrderSide.BUY, OrderType.LIMIT, new Price(100, 2), OrderQuantity.builder().originalQuantity(10).remainingQuantity(10).build(), TimeInForce.ALL_OR_NONE);

        when(orderBook.getBookBySide(OrderSide.BUY)).thenReturn(new TreeMap<>());
        when(orderBook.getBookBySide(OrderSide.SELL)).thenReturn(new TreeMap<>());

        // Act
        List<Event> events = matcher.match(orderBook, buyOrder);

        // Assert
        assertTrue(events.isEmpty());
    }
}