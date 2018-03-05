package com.rbc.exchange.api;

import com.rbc.exchange.matcher.OrderMatcher;
import com.rbc.exchange.storage.ExecutedStore;
import com.rbc.exchange.storage.OrderStore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {SMController.class, OrderStore.class, ExecutedStore.class, OrderMatcher.class})
@WebMvcTest(SMController.class)
public class SMControllerTest {
    @Autowired
    private MockMvc mvc;

    /* Test each endpoint with a clean data store. This will ensure no exceptions occur
     * during any calculation stage due to the lack of data. */

    @Test
    public void testNoDataOpenInterest() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/api/stockmatch/interest/VOD.L/SELL")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void testNoDataAvgExecutionPrice() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/api/stockmatch/execution/VOD.L/")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void testNoDataExecutedQuantity() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/api/stockmatch/quantity/VOD.L/User1/")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    /* Test adding data to the data store */

    @Test
    public void testInvalidOrderPayloadMissingField() throws Exception {
        //No Ric code should return 400 Bad Request
        postOrder("{\"direction\": \"SELL\"," +
                   "\"quantity\": 1000," +
                   "\"price\": \"100.20\"," +
                   "\"user\": \"User1\"}", status().is(400));
    }

    @Test
    public void testInvalidOrderPayloadMispelled() throws Exception {
        //Bad spelling of field should result in 400 Bad Request
        postOrder("{\"diretion\": \"SELL\"," +
                   "\"ricCode\": \"VOD.L\"," +
                   "\"quantity\": 1000," +
                   "\"price\": \"100.20\"," +
                   "\"user\": \"User1\"}", status().is(400));
    }

    @Test
    public void testValidPayload() throws Exception {
        //Verify that
        try {
            preFillToStageTestData(1);
        } finally {
            clearStore();
        }
    }

    /* Test each stage of the test data to verify it matches the specification */

    @Test
    public void testStep5() throws Exception {
        //Add the existing test steps and ensure that they are created correctly
        try {
            preFillToStageTestData(5);

            mvc.perform(MockMvcRequestBuilders.get("/api/stockmatch/interest/VOD.L/SELL")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.priceQuantity", hasSize(1)))
                    .andExpect(jsonPath("$.priceQuantity[0].pricePoint", is(102.00)))
                    .andExpect(jsonPath("$.priceQuantity[0].quantity", is(500)));

            mvc.perform(MockMvcRequestBuilders.get("/api/stockmatch/execution/VOD.L/")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.averagePrice", is(100.2000)));

            mvc.perform(MockMvcRequestBuilders.get("/api/stockmatch/quantity/VOD.L/User1/")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.quantity", is(-1000)));

            mvc.perform(MockMvcRequestBuilders.get("/api/stockmatch/quantity/VOD.L/User2/")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.quantity", is(1000)));
        } finally {
            clearStore();
        }
    }

    @Test
    public void testStep6() throws Exception {
        //Add the existing test steps and ensure that they are created correctly
        try {
            preFillToStageTestData(6);

            mvc.perform(MockMvcRequestBuilders.get("/api/stockmatch/execution/VOD.L/")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.averagePrice", is(101.1333)));

            mvc.perform(MockMvcRequestBuilders.get("/api/stockmatch/quantity/VOD.L/User1/")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.quantity", is(-500)));

            mvc.perform(MockMvcRequestBuilders.get("/api/stockmatch/quantity/VOD.L/User2/")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.quantity", is(500)));
        } finally {
            clearStore();
        }
    }

    @Test
    public void testStep7() throws Exception {
        //Add the existing test steps and ensure that they are created correctly
        try {
            preFillToStageTestData(7);

            mvc.perform(MockMvcRequestBuilders.get("/api/stockmatch/execution/VOD.L/")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.averagePrice", is(99.8800)));

            mvc.perform(MockMvcRequestBuilders.get("/api/stockmatch/quantity/VOD.L/User1/")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.quantity", is(500)));

            mvc.perform(MockMvcRequestBuilders.get("/api/stockmatch/quantity/VOD.L/User2/")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.quantity", is(-500)));
        } finally {
            clearStore();
        }
    }

    /** Utility methods */

    private void preFillToStageTestData(int stage) throws Exception {
        while (true) {
            postOrder("{\"direction\": \"SELL\",\"ricCode\": \"VOD.L\",\"quantity\": 1000,\"price\": \"100.20\"," +
                    "\"user\": \"User1\"}", status().isCreated());
            if (stage <= 1) {
                break;
            }
            postOrder("{\"direction\": \"BUY\",\"ricCode\": \"VOD.L\",\"quantity\": 1000,\"price\": \"100.20\"," +
                    "\"user\": \"User2\"}", status().isCreated());
            postOrder("{\"direction\": \"BUY\",\"ricCode\": \"VOD.L\",\"quantity\": 1000,\"price\": \"99.00\"," +
                    "\"user\": \"User1\"}", status().isCreated());
            postOrder("{\"direction\": \"BUY\",\"ricCode\": \"VOD.L\",\"quantity\": 1000,\"price\": \"101.00\"," +
                    "\"user\": \"User1\"}", status().isCreated());
            postOrder("{\"direction\": \"SELL\",\"ricCode\": \"VOD.L\",\"quantity\": 500,\"price\": \"102.00\"," +
                    "\"user\": \"User2\"}", status().isCreated());
            if (stage <= 5) {
                break;
            }
            postOrder("{\"direction\": \"BUY\",\"ricCode\": \"VOD.L\",\"quantity\": 500,\"price\": \"103.00\"," +
                    "\"user\": \"User1\"}", status().isCreated());
            if (stage <= 6) {
                break;
            }
            postOrder("{\"direction\": \"SELL\",\"ricCode\": \"VOD.L\",\"quantity\": 1000,\"price\": \"98.00\"," +
                    "\"user\": \"User2\"}", status().isCreated());
            break;
        }
    }

    private void postOrder(String jsonData, ResultMatcher matcher) throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/api/stockmatch/")
                .content(jsonData)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(matcher);
    }

    private void clearStore() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/api/stockmatch/clear")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
