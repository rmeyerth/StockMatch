package com.rbc.exchange.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PriceQuantity {
    private BigDecimal pricePoint;
    private int quantity;
}
