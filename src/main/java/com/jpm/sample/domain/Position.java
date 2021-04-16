package com.jpm.sample.domain;

import java.util.Objects;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Position {

    private String account;
    private String security;
    private Long quantity;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position)) return false;
        Position position = (Position) o;
        return Objects.equals(account, position.account) && Objects.equals(security, position.security);
    }

    @Override
    public int hashCode() {
        return Objects.hash(account, security);
    }
}
