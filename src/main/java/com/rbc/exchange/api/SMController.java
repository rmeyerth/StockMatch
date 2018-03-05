package com.rbc.exchange.api;

import com.rbc.exchange.model.Direction;
import com.rbc.exchange.model.Order;
import com.rbc.exchange.model.response.AvgExecPriceRic;
import com.rbc.exchange.model.response.ExecQuantity;
import com.rbc.exchange.model.response.OrderQuery;
import com.rbc.exchange.storage.ExecutedStore;
import com.rbc.exchange.storage.OrderStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
public class SMController {

    private OrderStore orderStore;
    private ExecutedStore executedStore;

    public SMController(@Autowired OrderStore orderStore, @Autowired ExecutedStore executedStore) {
        this.orderStore = orderStore;
        this.executedStore = executedStore;
    }

    @RequestMapping(value = "/api/stockmatch/", method = RequestMethod.POST)
    public ResponseEntity addOrder(@Valid @RequestBody Order newOrder) {
        orderStore.add(newOrder);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @RequestMapping(value = "/api/stockmatch/interest/{ricCode}/{direction}", method = RequestMethod.GET)
    public OrderQuery getOpenInterest(@PathVariable String ricCode, @PathVariable Direction direction) {
        return orderStore.queryByRicAndDirection(ricCode, direction);
    }

    @RequestMapping(value = "/api/stockmatch/execution/{ricCode}/", method = RequestMethod.GET)
    public AvgExecPriceRic getAverageExecutionPrice(@PathVariable String ricCode) {
        return executedStore.getAverageExecutionPriceForRic(ricCode);
    }

    @RequestMapping(value = "/api/stockmatch/quantity/{ricCode}/{user}/", method = RequestMethod.GET)
    public ExecQuantity getExecutedQuantity(@PathVariable String ricCode, @PathVariable String user) {
        return executedStore.getExecutedQuantityForRicAndUser(ricCode, user);
    }

    @RequestMapping(value = "/api/stockmatch/clear", method = RequestMethod.GET)
    public void getClearStore() {
        executedStore.resetStore();
        orderStore.resetStore();
    }
}
