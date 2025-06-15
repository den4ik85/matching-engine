package com.interview.sample.service;

import com.interview.sample.books.BookRepository;
import com.interview.sample.books.OrderBook;
import com.interview.sample.broker.EventBroker;
import com.interview.sample.controller.command.OrderCancelledCommand;
import com.interview.sample.domain.event.OrderCancelRejectedEvent;
import com.interview.sample.domain.event.OrderCancelledEvent;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;


import java.util.Optional;

@AllArgsConstructor
@Log4j2
@Component
public class OrderCancelledCommandHandler implements CommandHandler<OrderCancelledCommand> {

    private final BookRepository bookRepository;

    private final EventBroker eventBroker;

    @Override
    public void handle(OrderCancelledCommand command) {
        //Any additional business validation rules can be done here

        Optional<OrderBook> orderBook = bookRepository.find(command.securityId());
        if (orderBook.isEmpty()) {
            String errorMessage = "Order book not found for security: " + command.securityId();

            OrderCancelRejectedEvent rejectedEvent = new OrderCancelRejectedEvent(
                    command.clientOrderId(),
                    command.clientId(),
                    errorMessage);
            eventBroker.publish(rejectedEvent);
            log.error(errorMessage);
            return;
        }

        Optional<OrderCancelledEvent> orderCancelledEvent = orderBook.get().cancelOrder(command.clientOrderId());
        orderCancelledEvent.ifPresent(
                event -> {
                    eventBroker.publish(event);
                    log.info("Order cancelled event sent out: {}", event);
                }
        );
    }
}
