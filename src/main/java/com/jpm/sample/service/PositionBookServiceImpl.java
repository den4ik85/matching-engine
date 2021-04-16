package com.jpm.sample.service;

import com.jpm.sample.domain.EventType;
import com.jpm.sample.domain.Position;
import com.jpm.sample.domain.TradeEvent;
import com.jpm.sample.repository.PositionBookRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;


public class PositionBookServiceImpl implements PositionBookService {

    private final ReentrantLock lock = new ReentrantLock();

    @Override
    public void processEvent(TradeEvent event) throws Exception {
        //validation should be applied here (if account is exists and securities is acceptable etc.)

        try {
            lock.lock();
            switch (event.getEventType()) {
                case SELL:
                    sell(event);
                    break;
                case BUY:
                    buy(event);
                    break;
                case CANCEL:
                    cancelEvent(event);
                    break;
                default:
                    throw new Exception("Action type is not correct");
            }
        } finally {
            PositionBookRepository.eventHistory.put(event.getId(), event);
            lock.unlock();
        }
    }

    private void sell(TradeEvent event) throws Exception {
            Position position = Position.builder().account(event.getAccount()).security(event.getSecurity()).build();
            Long numberOfSecurities = getPositionQuantity(position);
            if (numberOfSecurities < event.getQuantity()) {
                throw new Exception("Insufficient amount of securities");
            }
            PositionBookRepository.processedEvents.compute(position, (pos, quantity) -> {
                if (quantity == null)
                    return event.getQuantity();
                else
                    return quantity - event.getQuantity();
            });
    }

    private void buy(TradeEvent event) {
        PositionBookRepository.processedEvents
                .compute(Position.builder().account(event.getAccount()).security(event.getSecurity()).build()
                        , (position, integer) -> {
            if (integer == null)
                return event.getQuantity();
            else
                return integer + event.getQuantity();
        });
    }

    private void cancelEvent(TradeEvent event) throws Exception {
            TradeEvent eventToCancel = PositionBookRepository.eventHistory.get(event.getId());
            if (eventToCancel.getEventType() == EventType.BUY) {
                sell(eventToCancel);
            } else if (eventToCancel.getEventType() == EventType.SELL) {
                buy(eventToCancel);
            }
    }

    @Override
    public Long getPositionQuantity(Position position) {
        return PositionBookRepository.processedEvents.get(position);
    }

    @Override
    public List<Position> getAllPositions() {
        List<Position> result = new ArrayList();
        PositionBookRepository.processedEvents.forEach((k,v) -> result.add(Position.builder().account(k.getAccount()).security(k.getSecurity()).quantity(v).build()));
        return result;
    }

}
