package com.rbc.exchange.model.response;

import com.rbc.exchange.model.Direction;
import lombok.Data;

import java.util.List;

@Data
public class OrderQuery {
    private String ricCode;
    private Direction direction;
    private List<PriceQuantity> priceQuantity;

    public OrderQuery(String ricCode, Direction direction) {
        this.ricCode = ricCode;
        this.direction = direction;
    }
}
