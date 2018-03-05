package com.rbc.exchange.matcher;

import com.rbc.exchange.model.Direction;
import com.rbc.exchange.model.Executed;
import com.rbc.exchange.model.Order;
import com.rbc.exchange.storage.ExecutedStore;
import com.rbc.exchange.storage.OrderStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMatcher extends Matcher<Order, Executed> {

    /**
     * Constructor for the Matcher class takes the store object as an argument so that it can
     * register itself for events. This uses the Observer pattern principle.
     * @param store The store that triggers matcher events
     * @param matchStore The store to which matches are persisted
     */
    public OrderMatcher(OrderStore store, ExecutedStore matchStore) {
        super(store, matchStore);
    }

    @Override
    public void notifyAddEvent(Order order) {
        //Collect the matches for the new order
        List<Order> matched = getDataStore().getItems()
                .stream()
                .filter(a -> a.getRicCode().equalsIgnoreCase(order.getRicCode()) &&
                            !a.getDirection().equals(order.getDirection()) &&
                             a.getQuantity() == order.getQuantity() &&
                             sellIsLessOrEqualToBuy(a, order) &&
                             notPartOfExistingMatch(a))
                .collect(Collectors.toList());
        if (matched.size() > 1) {
            handleMultipleMatches(matched, order);
        } else if (!matched.isEmpty()) {
            getMatchStore().add(createMatch(matched.get(0), order));
        }
    }

    private boolean sellIsLessOrEqualToBuy(Order first, Order second) {
        Order buy = (first.getDirection() == Direction.BUY) ? first : second;
        Order sell = (buy.equals(first)) ? second : first;
        return sell.getPrice().compareTo(buy.getPrice()) <= 0;
    }

    private boolean notPartOfExistingMatch(Order order) {
        List<Executed> found = getMatchStore().getItems()
                .stream()
                .filter(a -> a.getFirstOrder().getOrderId().equals(order.getOrderId()) ||
                             a.getSecondOrder().getOrderId().equals(order.getOrderId()))
                .collect(Collectors.toList());
        return found.isEmpty();
    }

    private void handleMultipleMatches(List<Order> matches, Order newOrder) {
        if (matchesSamePrice(matches)) {
            matches.sort((first, second) -> first.getCreated().compareTo(second.getCreated()));
            getMatchStore().add(createMatch(matches.get(0), newOrder));
        } else {
            matches.sort((first, second) -> first.getPrice().compareTo(second.getPrice()));
            //If the order is a sell then select the highest, otherwise lowest price
            int matchIndex = (newOrder.getDirection() == Direction.SELL) ? matches.size() - 1 : 0;
            getMatchStore().add(createMatch(matches.get(matchIndex), newOrder));
        }
    }

    private boolean matchesSamePrice(List<Order> matches) {
        for (int i = 0;i < matches.size() - 1;i++) {
            if (!matches.get(i).getPrice().equals(matches.get(i + 1).getPrice())) {
                return false;
            }
        }
        return true;
    }

    private Executed createMatch(Order existing, Order newOrder) {
        return new Executed(existing, newOrder, newOrder.getPrice());
    }
}
