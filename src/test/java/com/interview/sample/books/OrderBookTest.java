package com.interview.sample.books;

import com.interview.sample.domain.event.Event;
import com.interview.sample.domain.event.OrderCancelledEvent;
import com.interview.sample.domain.instrument.Instrument;
import com.interview.sample.domain.order.*;
import com.interview.sample.matcher.AuctionStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OrderBookTest {

    private OrderBook orderBook;
    @Mock
    private AuctionStrategy matcher;
    private Instrument instrument;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        OrderBookFactory orderBookFactory = new OrderBookFactory(matcher);
        instrument = new Instrument("123", "AAPL");
        orderBook = orderBookFactory.createOrderBook(instrument);
    }

    @Test
    void testPlaceLimitOrderUpdatesOrderBook() {
        // Arrange
        Order order = new Order("123", "client1", "order1", OrderSide.BUY, OrderType.LIMIT, new Price(100, 2), OrderQuantity.builder().originalQuantity(10).remainingQuantity(10).build(), TimeInForce.ALL_OR_NONE);

        when(matcher.match(orderBook, order)).thenReturn(List.of());

        // Act
        List<Event> events = orderBook.placeOrder(order);

        // Assert
        assertTrue(events.isEmpty());
        assertEquals(order, orderBook.getBookBySide(OrderSide.BUY).get(order.getPrice()).peek());
        assertEquals(order.getPrice(), orderBook.getBestBidPrice());
    }

    @Test
    void testCancelOrderRemovesOrderFromBook() {
        // Arrange
        Order order = new Order("123", "client1", "order1", OrderSide.BUY, OrderType.LIMIT, new Price(100, 2), OrderQuantity.builder().originalQuantity(10).remainingQuantity(10).build(), TimeInForce.ALL_OR_NONE);
        orderBook.placeOrder(order);

        // Act
        Optional<OrderCancelledEvent> cancelledEvent = orderBook.cancelOrder(order.getClientOrderId());

        // Assert
        assertTrue(cancelledEvent.isPresent());
        assertEquals(order.getClientOrderId(), cancelledEvent.get().getClientOrderId());
        assertNull(orderBook.getBookBySide(OrderSide.BUY).get(order.getPrice()));
        assertNull(orderBook.getBestBidPrice());
    }

    @Test
    void testGetMarketPriceReturnsMidPrice() {
        // Arrange
        Order buyOrder = new Order("123", "client1", "buyOrder", OrderSide.BUY, OrderType.LIMIT, new Price(100, 2), OrderQuantity.builder().originalQuantity(10).remainingQuantity(10).build(), TimeInForce.ALL_OR_NONE);
        Order sellOrder = new Order("123", "client2", "sellOrder", OrderSide.SELL, OrderType.LIMIT, new Price(200, 2), OrderQuantity.builder().originalQuantity(10).remainingQuantity(10).build(), TimeInForce.ALL_OR_NONE);
        orderBook.placeOrder(buyOrder);
        orderBook.placeOrder(sellOrder);

        // Act
        Optional<Price> marketPrice = orderBook.getMarketPrice();

        // Assert
        assertTrue(marketPrice.isPresent());
        assertEquals(new Price(150, 2), marketPrice.get());
    }
}