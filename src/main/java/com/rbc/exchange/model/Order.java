package com.rbc.exchange.model;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class Order {
    //An order consists of direction (buy/sell), RIC (Reuters Instrument Code), quantity, price and user
    private Long orderId;
    private Date created;

    @NotNull
    private Direction direction;
    @NotNull
    private String ricCode;
    @NotNull
    private int quantity;
    @NotNull
    private BigDecimal price;
    @NotNull
    private String user;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Order)) {
            return false;
        }
        Order order = (Order)o;
        return orderId.equals(order.getOrderId()) &&
               direction.equals(order.getDirection()) &&
               ricCode.equalsIgnoreCase(order.getRicCode()) &&
               quantity == order.getQuantity() &&
               price.equals(order.getPrice()) &&
               user.equalsIgnoreCase(order.getUser());
    }
}
