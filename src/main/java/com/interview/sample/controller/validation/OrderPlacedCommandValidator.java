package com.interview.sample.controller.validation;


import com.interview.sample.controller.command.OrderPlacedCommand;
import com.interview.sample.domain.order.OrderType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class OrderPlacedCommandValidator {

    public void validate(OrderPlacedCommand command) {
        if (command.securityId() == null || command.securityId().isBlank()) {
            throw new IllegalArgumentException("Security ID must not be null or blank");
        }
        if (command.clientId() == null || command.clientId().isBlank()) {
            throw new IllegalArgumentException("Client ID must not be null or blank");
        }
        if (command.clientOrderId() == null || command.clientOrderId().isBlank()) {
            throw new IllegalArgumentException("Client Order ID must not be null or blank");
        }
        if (command.orderType() == OrderType.LIMIT && (command.price() == null || command.price().compareTo(BigDecimal.ZERO) <= 0)) {
            throw new IllegalArgumentException("Price must be greater than zero for LIMIT orders");
        }
        if (command.quantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        if (command.orderType() == null) {
            throw new IllegalArgumentException("Order Type must not be null");
        }
    }
}