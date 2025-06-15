package com.interview.sample.domain.event;

import com.interview.sample.domain.order.Price;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TradeEvent implements Event {
    String buyOrderId;
    String sellOrderId;
    String instrumentId;
    Price price;
    int quantity;
    long timeStamp;

    @Override
    public EventType getType() {
        return EventType.TRADE;
    }
}