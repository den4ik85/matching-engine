package com.interview.sample.controller;
import com.interview.sample.controller.command.InstrumentCreatedCommand;
import com.interview.sample.controller.validation.InstrumentCreatedCommandValidator;
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

public class InstrumentControllerTest {

    @Mock
    private CommandExecutor commandExecutor;

    @Mock
    private InstrumentCreatedCommandValidator instrumentCreatedCommandValidator;

    @InjectMocks
    private InstrumentController instrumentController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldCreateInstrumentForValidCommand() {
        InstrumentCreatedCommand command = new InstrumentCreatedCommand("123", "AAPL");

        doNothing().when(instrumentCreatedCommandValidator).validate(command);

        ResponseEntity<String> response = instrumentController.createInstrument(command);

        assertEquals(ResponseEntity.accepted().body("Instrument creation submitted for security: 123"), response);
        verify(instrumentCreatedCommandValidator, times(1)).validate(command);
        verify(commandExecutor, times(1)).execute(command);
    }

    @Test
    void createInstrumentFailedForInvalidCommand() {
        InstrumentCreatedCommand command = new InstrumentCreatedCommand("", "");

        doThrow(new IllegalArgumentException("Invalid command")).when(instrumentCreatedCommandValidator).validate(command);

        assertThrows(IllegalArgumentException.class, () -> instrumentController.createInstrument(command), "Invalid command");

        verify(instrumentCreatedCommandValidator, times(1)).validate(command);
        verifyNoInteractions(commandExecutor);
    }
}