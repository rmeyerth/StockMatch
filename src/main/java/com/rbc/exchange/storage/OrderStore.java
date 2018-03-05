package com.rbc.exchange.storage;

import com.rbc.exchange.model.Direction;
import com.rbc.exchange.model.Executed;
import com.rbc.exchange.model.Order;
import com.rbc.exchange.model.response.OrderQuery;
import com.rbc.exchange.model.response.PriceQuantity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderStore extends Store<Order> {

    private List<Order> storage;

    private ExecutedStore executedStore;

    public OrderStore(@Autowired ExecutedStore executedStore) {
        storage = new ArrayList<>();
        this.executedStore = executedStore;
    }

    @Override
    public void add(Order value) {
        value.setOrderId(Integer.toUnsignedLong(storage.size()));
        value.setCreated(new Date());
        notifyAddEvent(value);
        storage.add(value);
    }

    public OrderQuery queryByRicAndDirection(String ricCode, Direction direction) {
        List<Order> filtered = storage
                .stream()
                .filter(a -> a.getRicCode().equalsIgnoreCase(ricCode) && a.getDirection().equals(direction))
                .filter(this::notPartOfExistingMatch)
                .collect(Collectors.toList());

        OrderQuery result = new OrderQuery(ricCode, direction);
        List<PriceQuantity> priceQuantities = new ArrayList<>();
        filtered.sort((first, second) -> first.getPrice().compareTo(second.getPrice()));
        BigDecimal pricePoint = (filtered.size() > 0) ? filtered.get(0).getPrice() : null;
        int sumQuantity = (filtered.size() > 0) ? filtered.get(0).getQuantity() : -1;
        for (int i = 1;i < filtered.size();i++) {
            Order current = filtered.get(i);
            if (!current.getPrice().equals(pricePoint)) {
                priceQuantities.add(new PriceQuantity(pricePoint, sumQuantity));
                pricePoint = current.getPrice();
                sumQuantity = current.getQuantity();
            } else {
                sumQuantity += current.getQuantity();
            }
        }
        if (pricePoint != null && sumQuantity != -1) {
            priceQuantities.add(new PriceQuantity(pricePoint, sumQuantity));
        }
        result.setPriceQuantity(priceQuantities);
        return result;
    }

    private boolean notPartOfExistingMatch(Order order) {
        List<Executed> found = executedStore.getItems()
                .stream()
                .filter(a -> a.getFirstOrder().getOrderId().equals(order.getOrderId()) ||
                        a.getSecondOrder().getOrderId().equals(order.getOrderId()))
                .collect(Collectors.toList());
        return found.isEmpty();
    }

    @Override
    public List<Order> getItems() {
        return storage;
    }

    @Override
    public void resetStore() {
        storage.clear();
    }
}
