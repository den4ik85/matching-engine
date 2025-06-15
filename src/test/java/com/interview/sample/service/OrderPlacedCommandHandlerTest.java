package com.interview.sample.service;
import com.interview.sample.books.BookRepository;
import com.interview.sample.books.OrderBook;
import com.interview.sample.broker.EventBroker;
import com.interview.sample.controller.command.OrderPlacedCommand;
import com.interview.sample.controller.transformer.OrderCommandTransformer;
import com.interview.sample.domain.event.Event;
import com.interview.sample.domain.event.OrderRejectedEvent;
import com.interview.sample.domain.event.TradeEvent;
import com.interview.sample.domain.order.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OrderPlacedCommandHandlerTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private EventBroker eventBroker;

    @Mock
    private OrderCommandTransformer orderCommandTransformer;

    @InjectMocks
    private OrderPlacedCommandHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testHandleValidCommand() {
        // Arrange
        OrderPlacedCommand command = new OrderPlacedCommand("123", "client1", "order1", OrderSide.BUY, BigDecimal.valueOf(100.00), 10,OrderType.LIMIT,TimeInForce.ALL_OR_NONE);
        OrderBook orderBook = mock(OrderBook.class);
        Order transformedOrder = mock(Order.class);
        List<Event> events = List.of(new TradeEvent("order1", "order2", "123", new Price(100, 2), 10, System.nanoTime()));

        when(bookRepository.find("123")).thenReturn(Optional.of(orderBook));
        when(orderCommandTransformer.transform(command)).thenReturn(transformedOrder);
        when(orderBook.placeOrder(transformedOrder)).thenReturn(events);

        // Act
        handler.handle(command);

        // Assert
        verify(bookRepository, times(1)).find("123");
        verify(orderCommandTransformer, times(1)).transform(command);
        verify(orderBook, times(1)).placeOrder(transformedOrder);
        verify(eventBroker, times(1)).publish(events.get(0));
    }

    @Test
    void testHandleCommandWithNonExistentOrderBook() {
        // Arrange
        OrderPlacedCommand command = new OrderPlacedCommand("999", "client1", "order1", OrderSide.BUY, BigDecimal.valueOf(100.00), 10,OrderType.LIMIT,TimeInForce.ALL_OR_NONE);

        when(bookRepository.find("999")).thenReturn(Optional.empty());

        // Act
        handler.handle(command);

        // Assert
        verify(bookRepository, times(1)).find("999");
        verify(eventBroker, times(1)).publish(argThat(event -> {
            assertTrue(event instanceof OrderRejectedEvent);
            OrderRejectedEvent rejectedEvent = (OrderRejectedEvent) event;
            assertEquals("order1", rejectedEvent.getClientOrderId());
            assertEquals("client1", rejectedEvent.getClientId());
            assertEquals("Order book not found for security: 999", rejectedEvent.getReason());
            return true;
        }));
    }

    @Test
    void testHandleCommandWithEmptyEvents() {
        // Arrange
        OrderPlacedCommand command = new OrderPlacedCommand("123", "client1", "order1", OrderSide.BUY, BigDecimal.valueOf(100.00), 10,OrderType.LIMIT,TimeInForce.ALL_OR_NONE);
        OrderBook orderBook = mock(OrderBook.class);
        Order transformedOrder = mock(Order.class);

        when(bookRepository.find("123")).thenReturn(Optional.of(orderBook));
        when(orderCommandTransformer.transform(command)).thenReturn(transformedOrder);
        when(orderBook.placeOrder(transformedOrder)).thenReturn(List.of());

        // Act
        handler.handle(command);

        // Assert
        verify(bookRepository, times(1)).find("123");
        verify(orderCommandTransformer, times(1)).transform(command);
        verify(orderBook, times(1)).placeOrder(transformedOrder);
        verify(eventBroker, never()).publish(any());
    }
}