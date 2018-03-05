package com.rbc.exchange.matcher;

import com.rbc.exchange.storage.ExecutedStore;
import com.rbc.exchange.storage.OrderStore;
import com.rbc.exchange.util.OrderUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import java.math.BigDecimal;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class OrderMatcherTest {

    private OrderStore orderStore;
    private ExecutedStore executedStore;
    private OrderMatcher orderMatcher;

    @Before
    public void setUp() {
        executedStore = new ExecutedStore();
        orderStore = new OrderStore(executedStore);
        orderMatcher = new OrderMatcher(orderStore, executedStore);
    }

    @Test
    public void testExactMatch() throws Exception {
        assertEquals(executedStore.getItems().size(), 0);
        orderStore.add(OrderUtil.createOrder("SELL", "VOD.L", 1000, new BigDecimal(55.0), "User1"));
        orderStore.add(OrderUtil.createOrder("BUY", "VOD.L", 1000, new BigDecimal(55.0), "User2"));
        assertEquals(executedStore.getItems().size(), 1);
    }

    @Test
    public void testMultiMatchLastCreated() throws Exception {
        assertEquals(executedStore.getItems().size(), 0);
        orderStore.add(OrderUtil.createOrder("SELL", "VOD.L", 1000, new BigDecimal(55.0), "User1"));
        orderStore.add(OrderUtil.createOrder("SELL", "VOD.L", 1000, new BigDecimal(55.0), "User2"));
        orderStore.add(OrderUtil.createOrder("BUY", "VOD.L", 1000, new BigDecimal(55.0), "User3"));
        assertEquals(executedStore.getItems().size(), 1);
        assertEquals(executedStore.getItems().get(0).getFirstOrder().getUser(), "User1");
    }

    @Test
    public void testMultiMatchSellOrderHighestPrice() throws Exception {
        assertEquals(executedStore.getItems().size(), 0);
        orderStore.add(OrderUtil.createOrder("BUY", "VOD.L", 1000, new BigDecimal(47.0), "User1"));
        orderStore.add(OrderUtil.createOrder("BUY", "VOD.L", 1000, new BigDecimal(50.0), "User2"));
        orderStore.add(OrderUtil.createOrder("SELL", "VOD.L", 1000, new BigDecimal(42.0), "User3"));
        assertEquals(executedStore.getItems().size(), 1);
        assertEquals(executedStore.getItems().get(0).getFirstOrder().getUser(), "User2");
    }

    @Test
    public void testMultiMatchBuyOrderLowestPrice() throws Exception {
        assertEquals(executedStore.getItems().size(), 0);
        orderStore.add(OrderUtil.createOrder("SELL", "VOD.L", 1000, new BigDecimal(45.0), "User1"));
        orderStore.add(OrderUtil.createOrder("SELL", "VOD.L", 1000, new BigDecimal(50.0), "User2"));
        orderStore.add(OrderUtil.createOrder("BUY", "VOD.L", 1000, new BigDecimal(55.0), "User3"));
        assertEquals(executedStore.getItems().size(), 1);
        assertEquals(executedStore.getItems().get(0).getFirstOrder().getUser(), "User1");
    }

    @Test
    public void testOrderNotApplicableForMultiMatch() {
        //After the initial match has been made, the match should be removed from future matches
        assertEquals(executedStore.getItems().size(), 0);
        orderStore.add(OrderUtil.createOrder("SELL", "VOD.L", 1000, new BigDecimal(50.0), "User1"));
        orderStore.add(OrderUtil.createOrder("BUY", "VOD.L", 1000, new BigDecimal(50.0), "User2"));
        orderStore.add(OrderUtil.createOrder("BUY", "VOD.L", 1000, new BigDecimal(50.0), "User3"));
        assertEquals(executedStore.getItems().size(), 1);
        assertEquals(executedStore.getItems().get(0).getFirstOrder().getUser(), "User1");
    }
}
