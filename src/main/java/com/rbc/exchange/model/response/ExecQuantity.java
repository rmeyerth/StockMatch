package com.rbc.exchange.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExecQuantity {
    private String ricCode;
    private String user;
    private int quantity;
}
