package com.rbc.exchange.storage;

import com.rbc.exchange.model.Direction;
import com.rbc.exchange.model.response.OrderQuery;
import com.rbc.exchange.util.OrderUtil;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class OrderStoreTest {
    private ExecutedStore executedStore;
    private OrderStore orderStore;

    @Before
    public void setUp() {
        this.executedStore = new ExecutedStore();
        this.orderStore = new OrderStore(executedStore);
    }

    @Test
    public void testNewOrderAndClear() {
        orderStore.add(OrderUtil.createOrder("SELL", "VOD.L", 1000, new BigDecimal(55.0), "User1"));
        assertEquals(orderStore.getItems().size(), 1);
        orderStore.resetStore();
        assertTrue(orderStore.getItems().isEmpty());
    }

    @Test
    public void testQueryQuantityAndPriceSell() {
        orderStore.add(OrderUtil.createOrder("SELL", "VOD.L", 1000, new BigDecimal(55.0), "User1"));
        OrderQuery orderQuery = orderStore.queryByRicAndDirection("VOD.L", Direction.SELL);
        assertEquals(orderQuery.getPriceQuantity().size(), 1);
        assertEquals(orderQuery.getPriceQuantity().get(0).getQuantity(), 1000);
        assertTrue(orderQuery.getPriceQuantity().get(0).getPricePoint().compareTo(new BigDecimal(55.0)) == 0);
    }

    @Test
    public void testQueryQuantityAndPriceBuy() {
        orderStore.add(OrderUtil.createOrder("BUY", "VOD.L", 1000, new BigDecimal(55.0), "User2"));
        orderStore.add(OrderUtil.createOrder("BUY", "VOD.L", 800, new BigDecimal(102.0), "User1"));
        orderStore.add(OrderUtil.createOrder("BUY", "VOD.L", 500, new BigDecimal(109.0), "User2"));
        OrderQuery orderQuery = orderStore.queryByRicAndDirection("VOD.L", Direction.BUY);
        assertEquals(orderQuery.getPriceQuantity().size(), 3);
        assertEquals(orderQuery.getPriceQuantity().get(0).getQuantity(), 1000);
        assertTrue(orderQuery.getPriceQuantity().get(0).getPricePoint().compareTo(new BigDecimal(55.0)) == 0);
        assertEquals(orderQuery.getPriceQuantity().get(1).getQuantity(), 800);
        assertTrue(orderQuery.getPriceQuantity().get(1).getPricePoint().compareTo(new BigDecimal(102.0)) == 0);
        assertEquals(orderQuery.getPriceQuantity().get(2).getQuantity(), 500);
        assertTrue(orderQuery.getPriceQuantity().get(2).getPricePoint().compareTo(new BigDecimal(109.0)) == 0);
    }

    @Test
    public void testQueryQuantityAndPriceBuyOtherProdType() {
        orderStore.add(OrderUtil.createOrder("BUY", "VOD.L", 1000, new BigDecimal(55.0), "User2"));
        orderStore.add(OrderUtil.createOrder("BUY", "VOD.L", 800, new BigDecimal(102.0), "User1"));
        orderStore.add(OrderUtil.createOrder("BUY", "SIL.M", 1000, new BigDecimal(95.2), "User1"));
        orderStore.add(OrderUtil.createOrder("BUY", "VOD.L", 500, new BigDecimal(109.0), "User2"));
        OrderQuery orderQuery = orderStore.queryByRicAndDirection("SIL.M", Direction.BUY);
        assertEquals(orderQuery.getPriceQuantity().size(), 1);
        assertEquals(orderQuery.getPriceQuantity().get(0).getQuantity(), 1000);
        assertTrue(orderQuery.getPriceQuantity().get(0).getPricePoint().compareTo(new BigDecimal(95.2)) == 0);
    }
}
