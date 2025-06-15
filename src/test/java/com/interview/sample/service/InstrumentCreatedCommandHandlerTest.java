package com.interview.sample.service;


import com.interview.sample.books.BookRepository;
import com.interview.sample.books.OrderBook;
import com.interview.sample.books.OrderBookFactory;
import com.interview.sample.controller.command.InstrumentCreatedCommand;
import com.interview.sample.domain.instrument.Instrument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.mockito.Mockito.*;

public class InstrumentCreatedCommandHandlerTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private OrderBookFactory orderBookFactory;

    @InjectMocks
    private InstrumentCreatedCommandHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testHandleValidCommandCreatesInstrumentAndOrderBook() {
        // Arrange
        InstrumentCreatedCommand command = new InstrumentCreatedCommand("123", "AAPL");
        Instrument instrument = new Instrument("123", "AAPL");
        OrderBook orderBook = mock(OrderBook.class);

        when(bookRepository.find("123")).thenReturn(Optional.empty());
        when(orderBookFactory.createOrderBook(instrument)).thenReturn(orderBook);

        // Act
        handler.handle(command);

        // Assert
        verify(bookRepository, times(1)).find("123");
        verify(orderBookFactory, times(1)).createOrderBook(instrument);
        verify(bookRepository, times(1)).add(orderBook);
    }

    @Test
    void testHandleCommandDoesNotCreateDuplicateOrderBook() {
        // Arrange
        InstrumentCreatedCommand command = new InstrumentCreatedCommand("123", "AAPL");
        OrderBook existingOrderBook = mock(OrderBook.class);

        when(bookRepository.find("123")).thenReturn(Optional.of(existingOrderBook));

        // Act
        handler.handle(command);

        // Assert
        verify(bookRepository, times(1)).find("123");
        verifyNoInteractions(orderBookFactory);
        verify(bookRepository, never()).add(any());
    }
}