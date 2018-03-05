package com.rbc.exchange.storage;

import com.rbc.exchange.model.Direction;
import com.rbc.exchange.model.Executed;
import com.rbc.exchange.model.Order;
import com.rbc.exchange.model.response.AvgExecPriceRic;
import com.rbc.exchange.model.response.ExecQuantity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ExecutedStore extends Store<Executed> {

    private List<Executed> storage;

    public ExecutedStore() {
        storage = new ArrayList<>();
    }

    @Override
    public void add(Executed value) {
        storage.add(value);
    }

    @Override
    public List<Executed> getItems() {
        return storage;
    }

    @Override
    public void resetStore() {
        storage.clear();
    }

    public AvgExecPriceRic getAverageExecutionPriceForRic(String ricCode) {
        List<BigDecimal> executedPrices = storage
                .stream()
                .filter(a -> a.getFirstOrder().getRicCode().equalsIgnoreCase(ricCode))
                .map(a -> a.getExecutionPrice().multiply(new BigDecimal(a.getFirstOrder().getQuantity())))
                .collect(Collectors.toList());
        BigDecimal result = new BigDecimal(0);
        for (BigDecimal current : executedPrices) {
            result = result.add(current);
        }
        int sum = storage
                .stream()
                .filter(a -> a.getFirstOrder().getRicCode().equalsIgnoreCase(ricCode))
                .mapToInt(a -> a.getFirstOrder().getQuantity())
                .sum();
        if (sum != 0) {
            return new AvgExecPriceRic(result.divide(new BigDecimal(sum), 4, BigDecimal.ROUND_DOWN), ricCode);
        } else {
            return new AvgExecPriceRic(new BigDecimal(0), ricCode);
        }
    }

    public ExecQuantity getExecutedQuantityForRicAndUser(String ricCode, String user) {
        List<Executed> matched = storage
                .stream()
                .filter(a -> a.getFirstOrder().getRicCode().equalsIgnoreCase(ricCode))
                .collect(Collectors.toList());
        int buyQuantitySum = getQuantitySum(matched, Direction.BUY, user);
        int sellQuantitySum = getQuantitySum(matched, Direction.SELL, user);
        return new ExecQuantity(ricCode, user, buyQuantitySum - sellQuantitySum);
    }

    private int getQuantitySum(List<Executed> matched, Direction direction, String user) {
        return matched
                .stream()
                .map(a -> a.getFirstOrder().getDirection() == direction ? a.getFirstOrder() : a.getSecondOrder())
                .filter(a -> a.getUser().equalsIgnoreCase(user))
                .mapToInt(Order::getQuantity)
                .sum();
    }
}
