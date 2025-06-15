package com.interview.sample.controller.validation;

import com.interview.sample.controller.command.OrderCancelledCommand;
import org.springframework.stereotype.Component;

@Component
public class OrderCancelledCommandValidator {
    // In production matching-engine component validation logic produce the correspondent event with the reason of failure from business prospective
    // but those attributes are essential for the command to be processed correctly, so we validate them here.
    public void validate(OrderCancelledCommand command) {
        if (command.clientId() == null || command.clientId().isBlank()) {
            throw new IllegalArgumentException("Client ID must not be null or blank");
        }
        if (command.clientOrderId() == null || command.clientOrderId().isBlank()) {
            throw new IllegalArgumentException("Client Order ID must not be null or blank");
        }
        if (command.securityId() == null || command.securityId().isBlank()) {
            throw new IllegalArgumentException("Security ID must not be null or blank");
        }
    }
}