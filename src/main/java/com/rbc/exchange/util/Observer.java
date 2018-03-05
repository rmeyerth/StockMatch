package com.rbc.exchange.util;

public interface Observer<T> {
    void notifyAddEvent(T value);
}
