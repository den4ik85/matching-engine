package com.interview.sample.service;

import com.interview.sample.books.BookRepository;
import com.interview.sample.books.OrderBook;
import com.interview.sample.books.OrderBookFactory;
import com.interview.sample.controller.command.InstrumentCreatedCommand;
import com.interview.sample.domain.instrument.Instrument;
import lombok.RequiredArgsConstructor;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Log4j2
public class InstrumentCreatedCommandHandler implements CommandHandler<InstrumentCreatedCommand> {

    private final BookRepository bookRepository;

    private final OrderBookFactory orderBookFactory;

    @Override
    public void handle(InstrumentCreatedCommand command) {
        log.debug("Handling InstrumentCreatedCommand: " + command);

        //Initial validation has to be done in OrderEntryGateway microservice and in entry point of the matching-engine microservice
        // Specific validation would be better to be done in separate responsibility service, but for simplicity we do it here
        if (bookRepository.find(command.securityId()).isPresent()) {
            log.warn("Order book already exists for instrument: " + command.securityId());
            return;
        }

        //Create instrument and order book
        Instrument instrument = new Instrument(command.securityId(), command.symbol());
        OrderBook orderBook = orderBookFactory.createOrderBook(instrument);
        bookRepository.add(orderBook);

        log.debug("Order book has been created for instrument: " + command.securityId());
    }
}