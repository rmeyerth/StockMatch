package com.rbc.exchange.matcher;

import com.rbc.exchange.storage.Store;
import com.rbc.exchange.util.Observer;

abstract class Matcher<T, L> implements Observer<T> {
    private Store<T> store;
    private Store<L> matchStore;

    Matcher(Store<T> store, Store<L> matchStore) {
        this.store = store;
        this.matchStore = matchStore;
        store.addObserver(this);
    }

    Store<T> getDataStore() {
        return store;
    }

    Store<L> getMatchStore() { return matchStore; }
}
