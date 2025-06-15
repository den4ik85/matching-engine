package com.interview.sample.books;

import com.interview.sample.domain.order.Order;
import lombok.Getter;

import java.util.ArrayDeque;
import java.util.Queue;

public class PriceLevel {

    @Getter
    private final Queue<Order> orders = new ArrayDeque<>();
    @Getter
    private long totalQuantity;

    public void addOrder(Order order) {
        orders.add(order);
        totalQuantity += order.getQuantity().getRemainingQuantity();
    }

    public void removeOrder(Order order) {
        if (orders.remove(order)) {
            totalQuantity -= order.getQuantity().getRemainingQuantity();
        }
    }

    public Order peek() {
        return orders.peek();
    }

    public boolean isEmpty() {
        return orders.isEmpty();
    }

}