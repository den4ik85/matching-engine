package com.interview.sample.controller.command;

import com.interview.sample.domain.order.OrderSide;
import com.interview.sample.domain.order.OrderType;
import com.interview.sample.domain.order.TimeInForce;

import java.math.BigDecimal;

public record OrderPlacedCommand(String securityId, String clientId, String clientOrderId, OrderSide side, BigDecimal price, long quantity, OrderType orderType, TimeInForce timeInForce) implements Command {
}
