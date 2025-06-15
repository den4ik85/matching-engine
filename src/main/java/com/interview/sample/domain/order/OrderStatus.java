package com.interview.sample.domain.order;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum OrderStatus {

    NEW(false),
    FILLED(true),
    PARTIALLY_FILLED(false),
    REJECTED(true),
    EXPIRED(true),
    SUSPENDED(false),
    CANCELLED(true);


    private final boolean finalStatus;


    public boolean isFinal() {
        return finalStatus;
    }
}
