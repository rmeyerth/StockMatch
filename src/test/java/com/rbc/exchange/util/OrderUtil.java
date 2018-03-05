package com.rbc.exchange.util;

import com.rbc.exchange.model.Direction;
import com.rbc.exchange.model.Order;

import java.math.BigDecimal;

public class OrderUtil {
    public static Order createOrder(String direction, String ricCode, int quantity, BigDecimal price, String user) {
        //Would be better to use Lombok Builder annotation. Jackson however has issues mapping with it
        Order newOrder = new Order();
        newOrder.setDirection(Direction.valueOf(direction));
        newOrder.setRicCode(ricCode);
        newOrder.setQuantity(quantity);
        newOrder.setPrice(price);
        newOrder.setUser(user);
        return newOrder;
    }
}
