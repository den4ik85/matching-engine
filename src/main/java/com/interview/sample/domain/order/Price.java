package com.interview.sample.domain.order;

import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;


/**
 * The {@code Price} class represents a fixed-point decimal value using a scaled {@code long}.
 * It is specifically designed for use in performance-critical components such as matching engines (ME)
 * in trading systems.
 *
 * <p>This class avoids the imprecision and GC overhead associated with {@code BigDecimal}
 * by storing values as scaled {@code long} integers. The {@code scale} determines the number
 * of decimal places (e.g., scale 4 means values are stored in ten-thousandths).
 *
 * <p>All operations (add, subtract, compare) require matching scales for safety and determinism.
 */
@Getter
public class Price implements Comparable<Price> {
    private final long value;
    private final int scale;

    public Price(long value, int scale) {
        this.value = value;
        this.scale = scale;
    }

    public static Price from(BigDecimal decimal, int scale) {
        BigDecimal scaled = decimal.setScale(scale, RoundingMode.HALF_UP);
        long scaledValue = scaled.movePointRight(scale).longValueExact();
        return new Price(scaledValue, scale);
    }

    public BigDecimal toBigDecimal() {
        return BigDecimal.valueOf(value, scale);
    }

    public Price add(Price other) {
        checkScale(other);
        return new Price(this.value + other.value, this.scale);
    }

    public Price subtract(Price other) {
        checkScale(other);
        return new Price(this.value - other.value, this.scale);
    }

    public Price multiply(long multiplier) {
        return new Price(this.value * multiplier, this.scale);
    }

    public int compareTo(Price other) {
        checkScale(other);
        return Long.compare(this.value, other.value);
    }

    private void checkScale(Price other) {
        if (this.scale != other.scale) {
            throw new IllegalArgumentException("Price scales do not match: " + this.scale + " != " + other.scale);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Price price = (Price) o;
        return value == price.value && scale == price.scale;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, scale);
    }

    @Override
    public String toString() {
        return toBigDecimal().toPlainString();
    }
}