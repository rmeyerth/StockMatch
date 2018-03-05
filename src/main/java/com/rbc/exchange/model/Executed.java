package com.rbc.exchange.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class Executed {
    private Order firstOrder;
    private Order secondOrder;
    private BigDecimal executionPrice;
}
