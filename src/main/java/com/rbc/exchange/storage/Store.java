package com.rbc.exchange.storage;

import com.rbc.exchange.util.Observer;
import java.util.ArrayList;
import java.util.List;

public abstract class Store<T> {
    private List<Observer> observers;

    Store() {
        observers = new ArrayList<>();
    }

    public abstract void add(T value);
    public abstract List<T> getItems();

    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    void notifyAddEvent(T value) {
        observers.forEach(a -> a.notifyAddEvent(value));
    }

    public abstract void resetStore();
}
