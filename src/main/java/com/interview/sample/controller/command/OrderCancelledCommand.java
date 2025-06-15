package com.interview.sample.controller.command;

public record OrderCancelledCommand(String clientId, String clientOrderId, String securityId) implements Command {
}
