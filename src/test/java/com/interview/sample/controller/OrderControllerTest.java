package com.interview.sample.controller;

import com.interview.sample.controller.command.OrderPlacedCommand;
import com.interview.sample.controller.command.OrderCancelledCommand;
import com.interview.sample.controller.validation.OrderCancelledCommandValidator;
import com.interview.sample.controller.validation.OrderPlacedCommandValidator;
import com.interview.sample.service.CommandExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class OrderControllerTest {

    @Mock
    private CommandExecutor commandExecutor;

    @Mock
    private OrderCancelledCommandValidator orderCancelledCommandValidator;

    @Mock
    private OrderPlacedCommandValidator orderPlacedCommandValidator;

    @InjectMocks
    private OrderController orderController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void submitOrderReturnsAcceptedResponseForValidCommand() {
        OrderPlacedCommand command = new OrderPlacedCommand(
                "security123", "client456", "order789", null, null, 100, null, null
        );

        doNothing().when(orderPlacedCommandValidator).validate(command);

        ResponseEntity<String> response = orderController.submitOrder(command);

        assertEquals(ResponseEntity.accepted().body("Order placement submitted for client: client456"), response);
        verify(orderPlacedCommandValidator, times(1)).validate(command);
        verify(commandExecutor, times(1)).execute(command);
    }

    @Test
    void submitOrderThrowsExceptionForInvalidCommand() {
        OrderPlacedCommand command = new OrderPlacedCommand(
                "", "", "", null, null, 0, null, null
        );

        doThrow(new IllegalArgumentException("Invalid command")).when(orderPlacedCommandValidator).validate(command);

        assertThrows(IllegalArgumentException.class, () -> orderController.submitOrder(command), "Invalid command");

        verify(orderPlacedCommandValidator, times(1)).validate(command);
        verifyNoInteractions(commandExecutor);
    }

    @Test
    void cancelOrderReturnsAcceptedResponseForValidCommand() {
        OrderCancelledCommand command = new OrderCancelledCommand("client456", "order789", "security123");

        doNothing().when(orderCancelledCommandValidator).validate(command);

        ResponseEntity<String> response = orderController.cancelOrder(command);

        assertEquals(ResponseEntity.accepted().body("Order cancellation submitted for client: client456"), response);
        verify(orderCancelledCommandValidator, times(1)).validate(command);
        verify(commandExecutor, times(1)).execute(command);
    }

    @Test
    void cancelOrderThrowsExceptionForInvalidCommand() {
        OrderCancelledCommand command = new OrderCancelledCommand("", "", "");

        doThrow(new IllegalArgumentException("Invalid command")).when(orderCancelledCommandValidator).validate(command);

        assertThrows(IllegalArgumentException.class, () -> orderController.cancelOrder(command), "Invalid command");

        verify(orderCancelledCommandValidator, times(1)).validate(command);
        verifyNoInteractions(commandExecutor);
    }
}