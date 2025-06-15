package com.interview.sample.service;

import com.interview.sample.books.BookRepository;
import com.interview.sample.books.OrderBook;
import com.interview.sample.broker.EventBroker;
import com.interview.sample.controller.command.OrderPlacedCommand;
import com.interview.sample.controller.transformer.OrderCommandTransformer;
import com.interview.sample.domain.event.Event;
import com.interview.sample.domain.event.OrderRejectedEvent;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;


import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Log4j2
@Component
public class OrderPlacedCommandHandler implements CommandHandler<OrderPlacedCommand> {

    private final BookRepository bookRepository;

    private final OrderCommandTransformer orderCommandTransformer;

    private final EventBroker eventBroker;

    @Override
    public void handle(OrderPlacedCommand command) {
        //Any additional business validation rules can be done here

        Optional<OrderBook> orderBook = bookRepository.find(command.securityId());
        if (orderBook.isEmpty()) {
            String errorMessage = "Order book not found for security: " + command.securityId();

            OrderRejectedEvent orderRejectedEvent = new OrderRejectedEvent(
                    command.clientOrderId(),
                    command.clientId(),
                    errorMessage);
            eventBroker.publish(orderRejectedEvent);
            log.error(errorMessage);
            return;
        }

        List<Event> events = orderBook.get().placeOrder(orderCommandTransformer.transform(command));
        events.forEach(event -> {
            eventBroker.publish(event);
            log.info("Order placed event sent out: {}", event);
        });


    }
}
