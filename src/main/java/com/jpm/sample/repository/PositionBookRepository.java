package com.jpm.sample.repository;

import com.jpm.sample.domain.Position;
import com.jpm.sample.domain.TradeEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PositionBookRepository {

    //EventHistory could be represented as a Kafka log or RDBMS depends on how events are business critical
    public static Map<Long, TradeEvent> eventHistory = new ConcurrentHashMap<>();

    //Pre-Calculated number of Securities
    public static Map<Position, Long> processedEvents = new ConcurrentHashMap<>();
}
