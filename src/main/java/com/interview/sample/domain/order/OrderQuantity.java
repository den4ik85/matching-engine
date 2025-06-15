package com.interview.sample.domain.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderQuantity {

    private int originalQuantity;
    private int remainingQuantity;

    public int getCumulativeQuantity() {
        return originalQuantity - remainingQuantity;
    }

    public int getCancelledQuantity() {
        return originalQuantity - remainingQuantity - getCumulativeQuantity();
    }

    public boolean isFullyFilled() {
        return remainingQuantity == 0 && getCumulativeQuantity() == originalQuantity;
    }

    public boolean isPartiallyFilled() {
        return getCumulativeQuantity() > 0 && remainingQuantity > 0;
    }

    public boolean isUnfilled() {
        return getCumulativeQuantity() == 0 && remainingQuantity == originalQuantity;
    }
}
