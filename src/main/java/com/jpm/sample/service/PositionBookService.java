package com.jpm.sample.service;

import com.jpm.sample.domain.Position;
import com.jpm.sample.domain.TradeEvent;

import java.util.List;

public interface PositionBookService {

    void processEvent(TradeEvent event) throws Exception;

    Long getPositionQuantity(Position position);

    List<Position> getAllPositions();
}
