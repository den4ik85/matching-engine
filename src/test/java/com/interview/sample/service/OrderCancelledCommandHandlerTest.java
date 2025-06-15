package com.interview.sample.service;


import com.interview.sample.books.BookRepository;
import com.interview.sample.books.OrderBook;
import com.interview.sample.broker.EventBroker;
import com.interview.sample.controller.command.OrderCancelledCommand;
import com.interview.sample.domain.event.OrderCancelRejectedEvent;
import com.interview.sample.domain.event.OrderCancelledEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class OrderCancelledCommandHandlerTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private EventBroker eventBroker;

    @InjectMocks
    private OrderCancelledCommandHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testHandleValidCommand() {
        // Arrange
        OrderCancelledCommand command = new OrderCancelledCommand("client1", "order1",  "123");
        OrderBook orderBook = mock(OrderBook.class);
        OrderCancelledEvent cancelledEvent = new OrderCancelledEvent("order1", "client1", "123");

        when(bookRepository.find("123")).thenReturn(Optional.of(orderBook));
        when(orderBook.cancelOrder("order1")).thenReturn(Optional.of(cancelledEvent));

        // Act
        handler.handle(command);

        // Assert
        verify(bookRepository, times(1)).find("123");
        verify(orderBook, times(1)).cancelOrder("order1");
        verify(eventBroker, times(1)).publish(cancelledEvent);
    }

    @Test
    void testHandleCommandWithNonExistentOrderBook() {
        // Arrange
        OrderCancelledCommand command = new OrderCancelledCommand("client1", "order1",  "999");

        when(bookRepository.find("999")).thenReturn(Optional.empty());

        // Act
        handler.handle(command);

        // Assert
        verify(bookRepository, times(1)).find("999");
        verify(eventBroker, times(1)).publish(argThat(event -> {
            assertTrue(event instanceof OrderCancelRejectedEvent);
            OrderCancelRejectedEvent rejectedEvent = (OrderCancelRejectedEvent) event;
            assertEquals("order1", rejectedEvent.getClientOrderId());
            assertEquals("client1", rejectedEvent.getClientId());
            assertEquals("Order book not found for security: 999", rejectedEvent.getReason());
            return true;
        }));
    }

    @Test
    void testHandleCommandWithNoCancelledEvent() {
        // Arrange
        OrderCancelledCommand command = new OrderCancelledCommand("client1", "order1",  "123");
        OrderBook orderBook = mock(OrderBook.class);

        when(bookRepository.find("123")).thenReturn(Optional.of(orderBook));
        when(orderBook.cancelOrder("order1")).thenReturn(Optional.empty());

        // Act
        handler.handle(command);

        // Assert
        verify(bookRepository, times(1)).find("123");
        verify(orderBook, times(1)).cancelOrder("order1");
        verify(eventBroker, never()).publish(any());
    }
}