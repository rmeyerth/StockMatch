package com.rbc.exchange.storage;

import com.rbc.exchange.model.Direction;
import com.rbc.exchange.model.Executed;
import com.rbc.exchange.model.Order;
import com.rbc.exchange.model.response.AvgExecPriceRic;
import com.rbc.exchange.model.response.ExecQuantity;
import com.rbc.exchange.util.OrderUtil;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class ExecutedStoreTest {
    private ExecutedStore executedStore;

    @Before
    public void setUp() {
        this.executedStore = new ExecutedStore();
    }

    @Test
    public void testSimpleAddAndClear() {
        Order first = OrderUtil.createOrder("SELL", "VOD.L", 1000, new BigDecimal(55.0), "User1");
        Order second = OrderUtil.createOrder("BUY", "VOD.L", 1000, new BigDecimal(55.0), "User2");
        executedStore.add(new Executed(first, second, second.getPrice()));
        assertEquals(executedStore.getItems().size(), 1);
        executedStore.resetStore();
        assertTrue(executedStore.getItems().isEmpty());
    }

    @Test
    public void testQueriesEmptyStore() {
        AvgExecPriceRic avgExecPriceRic = executedStore.getAverageExecutionPriceForRic("VOD.L");
        assertTrue(avgExecPriceRic.getAveragePrice().compareTo(new BigDecimal(0)) == 0);
        ExecQuantity execQuantity = executedStore.getExecutedQuantityForRicAndUser("VOD.L", "User1");
        assertEquals(execQuantity.getQuantity(), 0);
    }

    @Test
    public void testAverageExecutionPrice() {
        Order first = OrderUtil.createOrder("SELL", "VOD.L", 1000, new BigDecimal(100.0), "User1");
        Order second = OrderUtil.createOrder("BUY", "VOD.L", 1000, new BigDecimal(103.0), "User2");
        executedStore.add(new Executed(first, second, second.getPrice()));
        first = OrderUtil.createOrder("SELL", "VOD.L", 500, new BigDecimal(50.0), "User1");
        second = OrderUtil.createOrder("BUY", "VOD.L", 500, new BigDecimal(55.0), "User2");
        executedStore.add(new Executed(first, second, second.getPrice()));
        AvgExecPriceRic avgExecPriceRic = executedStore.getAverageExecutionPriceForRic("VOD.L");
        //103 * 1000 + 55.0 * 500 = 130,500 / 1500 = 87 (Weighted average execution price)
        assertTrue(avgExecPriceRic.getAveragePrice().compareTo(new BigDecimal(87)) == 0);
    }

    @Test
    public void testExecutedQuantity() {
        Order first = OrderUtil.createOrder("SELL", "VOD.L", 1000, new BigDecimal(100.0), "User1");
        Order second = OrderUtil.createOrder("BUY", "VOD.L", 1000, new BigDecimal(103.0), "User2");
        executedStore.add(new Executed(first, second, second.getPrice()));
        first = OrderUtil.createOrder("SELL", "VOD.L", 500, new BigDecimal(50.0), "User1");
        second = OrderUtil.createOrder("BUY", "VOD.L", 500, new BigDecimal(55.0), "User2");
        executedStore.add(new Executed(first, second, second.getPrice()));

        ExecQuantity user1Quantity = executedStore.getExecutedQuantityForRicAndUser("VOD.L", "User1");
        assertEquals(user1Quantity.getQuantity(), -1500);
        ExecQuantity user2Quantity = executedStore.getExecutedQuantityForRicAndUser("VOD.L", "User2");
        assertEquals(user2Quantity.getQuantity(), 1500);
    }
}
