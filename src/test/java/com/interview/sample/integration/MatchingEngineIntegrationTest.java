package com.interview.sample.integration;

import com.interview.sample.books.BookRepository;
import com.interview.sample.books.OrderBook;
import com.interview.sample.broker.EventBroker;
import com.interview.sample.domain.event.Event;
import com.interview.sample.domain.event.OrderCancelledEvent;
import com.interview.sample.domain.event.OrderRejectedEvent;
import com.interview.sample.domain.event.TradeEvent;
import com.interview.sample.domain.order.OrderSide;
import com.interview.sample.service.CommandExecutor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest
@AutoConfigureMockMvc
public class MatchingEngineIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private EventBroker eventBroker;
    @Autowired
    private CommandExecutor commandExecutor;

    BlockingQueue<Event> capturedEvents;


    @BeforeEach
    public void init() throws InterruptedException {
        capturedEvents = new LinkedBlockingQueue<>();
        eventBroker.subscribe(capturedEvents::add);

    }

    @AfterEach
    public void clean() throws InterruptedException {
        bookRepository.clear();
        eventBroker.clear();

    }

    @Test
    void testInstrumentCreation() throws Exception {
        // Step 1: Create an instrument
        String instrumentJson = "{\"securityId\":\"a123\",\"symbol\":\"AAPL\"}";
        mockMvc.perform(post("/instruments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(instrumentJson))
                .andExpect(status().isAccepted());

        // Verify instrument creation
        assertTrue(bookRepository.find("a123").isPresent());
    }

    @Test
    void testOrderPlacement() throws Exception {
        // Step 1: Create an instrument
        String instrumentJson = "{\"securityId\":\"a1234\",\"symbol\":\"AAPL\"}";
        mockMvc.perform(post("/instruments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(instrumentJson))
                .andExpect(status().isAccepted());

        // Verify instrument creation
        assertTrue(bookRepository.find("a1234").isPresent());

        // Step 2: Place an order
        String orderJson = "{\"securityId\":\"a1234\",\"clientId\":\"client1\",\"clientOrderId\":\"order1\",\"side\":\"BUY\",\"price\":100,\"quantity\":10,\"orderType\":\"LIMIT\"}";
        mockMvc.perform(post("/orders/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andExpect(status().isAccepted());

        // Verify order placement
        OrderBook orderBook = bookRepository.find("a1234").get();
        assertEquals(1, orderBook.getBookBySide(OrderSide.BUY).size());
    }

    @Test
    void testOrderMatching() throws Exception {
        // Step 1: Create an instrument
        String instrumentJson = "{\"securityId\":\"a1235\",\"symbol\":\"AAPL\"}";
        mockMvc.perform(post("/instruments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(instrumentJson))
                .andExpect(status().isAccepted());

        // Step 2: Place a buy order
        String buyOrderJson = "{\"securityId\":\"a1235\",\"clientId\":\"client1\",\"clientOrderId\":\"order1\",\"side\":\"BUY\",\"price\":100,\"quantity\":10,\"orderType\":\"LIMIT\",\"timeInForce\":\"ALL_OR_NONE\"}";
        mockMvc.perform(post("/orders/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buyOrderJson))
                .andExpect(status().isAccepted());
        Thread.sleep(100);

        // Step 3: Place a sell order
        String sellOrderJson = "{\"securityId\":\"a1235\",\"clientId\":\"client2\",\"clientOrderId\":\"order2\",\"side\":\"SELL\",\"price\":100,\"quantity\":10,\"orderType\":\"MARKET\",\"timeInForce\":\"FILL_OR_KILL\"}";
        mockMvc.perform(post("/orders/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sellOrderJson))
                .andExpect(status().isAccepted());

        // Verification stage
        Thread.sleep(100);
        // Assert the captured events
        assertEquals(1, capturedEvents.size());
        Event event = capturedEvents.poll();
        assertTrue(event instanceof TradeEvent);
        TradeEvent tradeEvent = (TradeEvent) event;
        assertEquals(new BigDecimal("100").setScale(2), tradeEvent.getPrice().toBigDecimal());
        assertEquals(10, tradeEvent.getQuantity());
    }

    @Test
    void testOrderRejectedEvent() throws Exception {
        // Step 1: Place an order for a non-existent instrument
        String invalidOrderJson = "{\"securityId\":\"a999999\",\"clientId\":\"client1\",\"clientOrderId\":\"order1\",\"side\":\"BUY\",\"price\":100777,\"quantity\":10,\"orderType\":\"LIMIT\",\"timeInForce\":\"ALL_OR_NONE\"}";
        mockMvc.perform(post("/orders/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidOrderJson))
                .andExpect(status().isAccepted());

        Thread.sleep(100);
        // Assert the captured events
        assertEquals(1, capturedEvents.size());
        Event event = capturedEvents.poll();
        assertTrue(event instanceof OrderRejectedEvent);
        OrderRejectedEvent rejectedEvent = (OrderRejectedEvent) event;
        assertEquals("order1", rejectedEvent.getClientOrderId());
        assertEquals("client1", rejectedEvent.getClientId());
        assertEquals("Order book not found for security: a999999", rejectedEvent.getReason());
    }

    @Test
    void testOrderCancelledEvent() throws Exception {
        // Step 1: Create an instrument
        String instrumentJson = "{\"securityId\":\"a1239\",\"symbol\":\"AAPL\"}";
        mockMvc.perform(post("/instruments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(instrumentJson))
                .andExpect(status().isAccepted());

        // Step 2: Place a buy order
        String buyOrderJson = "{\"securityId\":\"a1239\",\"clientId\":\"client1\",\"clientOrderId\":\"order1\",\"side\":\"BUY\",\"price\":100,\"quantity\":10,\"orderType\":\"LIMIT\",\"timeInForce\":\"ALL_OR_NONE\"}";
        mockMvc.perform(post("/orders/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buyOrderJson))
                .andExpect(status().isAccepted());

        // Step 3: Cancel the order
        String cancelOrderJson = "{\"securityId\":\"a1239\",\"clientId\":\"client1\",\"clientOrderId\":\"order1\"}";
        mockMvc.perform(post("/orders/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cancelOrderJson))
                .andExpect(status().isAccepted());

        Thread.sleep(100);
        // Assert the captured events
        assertEquals(1, capturedEvents.size());
        Event event = capturedEvents.poll();
        assertTrue(event instanceof OrderCancelledEvent);
        OrderCancelledEvent cancelledEvent = (OrderCancelledEvent) event;
        assertEquals("order1", cancelledEvent.getClientOrderId());
        assertEquals("client1", cancelledEvent.getClientId());
    }

    @Test
    void testConcurrentOrderMatching() throws Exception {
        // Step 1: Create 10 distinct instruments
        for (int i = 1; i <= 10; i++) {
            String instrumentJson = String.format("{\"securityId\":\"%d\",\"symbol\":\"SYM%d\"}", i, i);
            mockMvc.perform(post("/instruments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(instrumentJson))
                    .andExpect(status().isAccepted());
        }

        // Step 2: Prepare orders (1000 BUY and 1000 SELL for each instrument)
        List<String> limitOrderRequests = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            for (int j = 1; j <= 50; j++) {
                String buyOrderJson = String.format("{\"securityId\":\"%d\",\"clientId\":\"client%d\",\"clientOrderId\":\"buy%d_%d\",\"side\":\"BUY\",\"price\":100,\"quantity\":10,\"orderType\":\"LIMIT\",\"timeInForce\":\"ALL_OR_NONE\"}", i, j, i, j);
                limitOrderRequests.add(buyOrderJson);
            }
        }

        List<String> marketOrderRequests = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            for (int j = 51; j <= 100; j++) {
                String sellOrderJson = String.format("{\"securityId\":\"%d\",\"clientId\":\"client%d\",\"clientOrderId\":\"sell%d_%d\",\"side\":\"SELL\",\"price\":100,\"quantity\":10,\"orderType\":\"MARKET\",\"timeInForce\":\"FILL_OR_KILL\"}", i, j, i, j);
                marketOrderRequests.add(sellOrderJson);
            }
        }

        // Step 3: Execute orders concurrently using ThreadPoolExecutor
        ExecutorService executor = Executors.newFixedThreadPool(100);
        for (String orderJson : limitOrderRequests) {
            executor.submit(() -> {
                try {
                    mockMvc.perform(post("/orders/submit")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(orderJson))
                            .andExpect(status().isAccepted());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
        // Wait for events to be published to Order book
        Thread.sleep(500);

        for (String orderJson : marketOrderRequests) {
            executor.submit(() -> {
                try {
                    mockMvc.perform(post("/orders/submit")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(orderJson))
                            .andExpect(status().isAccepted());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        // Shutdown executor and wait for completion
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        // Wait for events to be published
        Thread.sleep(500);

        // Assert the captured events
        assertEquals(500, capturedEvents.size());
        for (int i = 0; i < 500; i++) {
            Event event = capturedEvents.poll();
            validateTradeEvent(event);
        }
    }

    private void validateTradeEvent(Event event) {
        assertTrue(event instanceof TradeEvent);
        TradeEvent tradeEvent = (TradeEvent) event;
        assertEquals(new BigDecimal("100").setScale(2), tradeEvent.getPrice().toBigDecimal());
        assertEquals(10, tradeEvent.getQuantity());
    }
}