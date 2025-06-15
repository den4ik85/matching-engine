package com.interview.sample.controller;

import com.interview.sample.controller.command.OrderCancelledCommand;
import com.interview.sample.controller.command.OrderPlacedCommand;
import com.interview.sample.controller.validation.OrderCancelledCommandValidator;
import com.interview.sample.controller.validation.OrderPlacedCommandValidator;
import com.interview.sample.service.CommandExecutor;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.*;


@RestController
@RequestMapping("/orders")
@AllArgsConstructor
public class OrderController {

    private final CommandExecutor commandExecutor;

    private final OrderCancelledCommandValidator orderCancelledCommandValidator;

    private final OrderPlacedCommandValidator orderPlacedCommandValidator;


    @PostMapping("/submit")
    public ResponseEntity<String> submitOrder(@RequestBody OrderPlacedCommand command) {
        orderPlacedCommandValidator.validate(command);
        commandExecutor.execute(command);
        return ResponseEntity.accepted().body("Order placement submitted for client: " + command.clientId());
    }

    @PostMapping("/cancel")
    public ResponseEntity<String> cancelOrder(@RequestBody OrderCancelledCommand command) {
        orderCancelledCommandValidator.validate(command);
        commandExecutor.execute(command);
        return ResponseEntity.accepted().body("Order cancellation submitted for client: " + command.clientId());
    }
}