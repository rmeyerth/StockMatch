package com.rbc.exchange.model;

public enum Direction {
    BUY("buy"),
    SELL("sell");

    private String value;

    Direction(String direction) {
        this.value = direction;
    }
}
