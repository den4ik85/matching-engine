package com.interview.sample.domain.instrument;

import com.interview.sample.domain.order.Price;
import lombok.Data;

@Data
public class Instrument {
    private final String securityId;
    private final String symbol;
    private Price marketPrice;

    public void updateMarketPrice(Price bestBuy, Price bestSell) {
        if (bestBuy != null && bestSell != null) {
            long midValue = (bestBuy.getValue() + bestSell.getValue()) / 2;
            marketPrice = new Price(midValue, bestBuy.getScale());
        }
    }
    public Price getMarketPrice() { return marketPrice; }

}