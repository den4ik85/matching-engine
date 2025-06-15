package com.interview.sample.controller.transformer;

import com.interview.sample.controller.command.OrderPlacedCommand;
import com.interview.sample.domain.order.*;
import org.springframework.stereotype.Component;

@Component
public class OrderCommandTransformer {

    public Order transform(OrderPlacedCommand command) {
        Price price = Price.from(command.price(), 2);
        OrderQuantity quantity = OrderQuantity.builder()
                .originalQuantity((int) command.quantity())
                .remainingQuantity((int) command.quantity())
                .build();

        return new Order(
                command.securityId(),
                command.clientId(),
                command.clientOrderId(),
                command.side(),
                command.orderType(),
                price,
                quantity,
                command.timeInForce());
    }
}