package com.interview.sample.domain.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class OrderRejectedEvent implements Event {
    private final String clientOrderId;
    private final String clientId;
    private final String reason;

    @Override
    public EventType getType() {
        return EventType.ORDER_REJECTED;
    }
}