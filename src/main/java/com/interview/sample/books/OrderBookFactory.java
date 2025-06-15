package com.interview.sample.books;

import com.interview.sample.domain.instrument.Instrument;
import com.interview.sample.matcher.AuctionStrategy;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class OrderBookFactory {

    private final AuctionStrategy matcher;

    public OrderBook createOrderBook(Instrument instrument) {
        return OrderBook.builder()
                .instrument(instrument)
                .matcher(matcher)
                .build();
    }
}