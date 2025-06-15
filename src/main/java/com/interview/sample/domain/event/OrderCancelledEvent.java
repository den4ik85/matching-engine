package com.interview.sample.domain.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class OrderCancelledEvent implements Event {
    private final String clientOrderId;
    private final String clientId;
    private final String instrumentId;

    @Override
    public EventType getType() {
        return EventType.ORDER_CANCELLED;
    }
}