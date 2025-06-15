package com.interview.sample.controller.validation;


import com.interview.sample.controller.command.InstrumentCreatedCommand;
import org.springframework.stereotype.Component;

@Component
public class InstrumentCreatedCommandValidator {

    // In production matching-engine component validation logic produce the correspondent event with the reason of failure from business prospective
    // but those attributes are essential for the command to be processed correctly, so we validate them here.
    public void validate(InstrumentCreatedCommand command) {
        if (command.securityId() == null || command.securityId().isBlank()) {
            throw new IllegalArgumentException("Security ID must not be null or blank");
        }
        if (command.symbol() == null || command.symbol().isBlank()) {
            throw new IllegalArgumentException("Symbol must not be null or blank");
        }
    }
}