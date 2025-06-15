package com.interview.sample.controller;

import com.interview.sample.controller.command.InstrumentCreatedCommand;
import com.interview.sample.controller.validation.InstrumentCreatedCommandValidator;
import com.interview.sample.service.CommandExecutor;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/instruments")
@AllArgsConstructor
public class InstrumentController {

    private final CommandExecutor commandExecutor;

    private final InstrumentCreatedCommandValidator instrumentCreatedCommandValidator;

    @PostMapping
    public ResponseEntity<String> createInstrument(@RequestBody InstrumentCreatedCommand command) {
        instrumentCreatedCommandValidator.validate(command);
        commandExecutor.execute(command);
        return ResponseEntity.accepted().body("Instrument creation submitted for security: " + command.securityId());
    }
}