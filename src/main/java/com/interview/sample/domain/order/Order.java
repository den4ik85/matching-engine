package com.interview.sample.domain.order;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode(of = "clientOrderId")
public class Order {
    private final String clientId;
    private final String clientOrderId;
    private final String orderId;
    private final String instrumentId;
    private final OrderSide side;
    private final OrderType type;
    private final Price price;
    private OrderQuantity quantity;
    private OrderStatus status;
    TimeInForce timeInForce;

    public Order(String instrumentId, String clientId, String clientOrderId, OrderSide side, OrderType type, Price price, OrderQuantity quantity, TimeInForce timeInForce) {
        this.orderId = UUID.randomUUID().toString();
        this.instrumentId = instrumentId;
        this.clientOrderId = clientOrderId;
        this.clientId = clientId;
        this.side = side;
        this.type = type;
        this.price = price;
        this.quantity = quantity;
        this.status = OrderStatus.NEW;
        this.timeInForce = timeInForce;
    }
}