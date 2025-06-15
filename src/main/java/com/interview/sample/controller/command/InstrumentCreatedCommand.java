package com.interview.sample.controller.command;

public record InstrumentCreatedCommand(String securityId, String symbol) implements Command {

}