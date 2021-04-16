package com.jpm.sample.domain;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TradeEvent {

    private Long id;
    private EventType eventType;
    private String account;
    private String security;
    private Long quantity;

}
