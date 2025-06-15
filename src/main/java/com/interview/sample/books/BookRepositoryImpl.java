package com.interview.sample.books;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class BookRepositoryImpl implements BookRepository {

    private final Map<String, OrderBook> storage = new ConcurrentHashMap<>();

    @Override
    public boolean add(OrderBook book) {
        return storage.putIfAbsent(book.getInstrument().getSecurityId(), book) == null;
    }

    @Override
    public Optional<OrderBook> find(String securityId) {
        return Optional.ofNullable(storage.get(securityId));
    }

    @Override
    public boolean update(String securityId, OrderBook book) {
        return storage.replace(securityId, book) != null;
    }

    @Override
    public Optional<OrderBook> remove(OrderBook books) {
        boolean removed = storage.remove(books.getInstrument().getSecurityId(), books);
        return removed ? Optional.of(books) : Optional.empty();
    }

    @Override
    public void clear() {
        storage.clear();
    }
}