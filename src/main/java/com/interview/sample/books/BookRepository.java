package com.interview.sample.books;

import java.util.Optional;

public interface BookRepository {
    boolean add(OrderBook books);

    Optional<OrderBook> find(String securityId);

    boolean update(String securityId, OrderBook books);

    Optional<OrderBook> remove(OrderBook books);

    void clear();

}
