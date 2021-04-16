import com.jpm.sample.domain.EventType;
import com.jpm.sample.domain.Position;
import com.jpm.sample.domain.TradeEvent;
import com.jpm.sample.repository.PositionBookRepository;
import com.jpm.sample.service.PositionBookServiceImpl;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.runners.MockitoJUnitRunner;


import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;


@RunWith(MockitoJUnitRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PositionBookServiceImplTest {

    private PositionBookServiceImpl service;

    @Before
    public void setUp() {
        service = new PositionBookServiceImpl();
        PositionBookRepository.eventHistory.clear();
        PositionBookRepository.processedEvents.clear();
    }

    @Test
    public void shouldProcessBuyEvent () throws Exception {
        String account = "ACC1";
        String security = "SEC1";
        service.processEvent(TradeEvent.builder().id(1L).eventType(EventType.BUY).account(account).security(security).quantity(100L).build());
        service.processEvent(TradeEvent.builder().id(2L).eventType(EventType.BUY).account(account).security(security).quantity(50L).build());
        long quantity = service.getPositionQuantity(Position.builder().account(account).security(security).build());
        int procesedEvents = PositionBookRepository.eventHistory.size();
        List<Position> positions = service.getAllPositions();
        assertEquals("Expected: " + 150 + " amount of securities, got: " + quantity, 150, quantity);
        assertEquals("Expected: " + 2 + " processed events, got: " + procesedEvents, 2, procesedEvents);
        assertEquals("Expected: " + 1 + " positions, got: " + positions.size(), 1, positions.size());
    }

    @Test
    public void shouldProcessBuyEventForDifferentSecurities () throws Exception {
        String account = "ACC1";
        String account1 = "ACC2";
        String security = "SEC1";
        String security1 = "SECXYZ";
        service.processEvent(TradeEvent.builder().id(3L).eventType(EventType.BUY).account(account).security(security).quantity(12L).build());
        service.processEvent(TradeEvent.builder().id(4L).eventType(EventType.BUY).account(account).security(security1).quantity(50L).build());
        service.processEvent(TradeEvent.builder().id(5L).eventType(EventType.BUY).account(account1).security(security1).quantity(33L).build());
        service.processEvent(TradeEvent.builder().id(6L).eventType(EventType.BUY).account(account).security(security).quantity(20L).build());

        long quantity1 = service.getPositionQuantity(Position.builder().account(account).security(security).build());
        long quantity2 = service.getPositionQuantity(Position.builder().account(account1).security(security1).build());
        long quantity3 = service.getPositionQuantity(Position.builder().account(account).security(security1).build());
        int procesedEvents = PositionBookRepository.eventHistory.size();
        assertEquals("Expected: " + 32 + " amount of securities, got: " + quantity1, 32, quantity1);
        assertEquals("Expected: " + 33 + " amount of securities, got: " + quantity2, 33, quantity2);
        assertEquals("Expected: " + 50 + " amount of securities, got: " + quantity3, 50, quantity3);
        assertEquals("Expected: " + 4 + " processed events, got: " + procesedEvents, 4, procesedEvents);
    }

    @Test
    public void shouldProcessCancelEvent () throws Exception {
        String account = "ACC1";
        String security = "SEC1";
        service.processEvent(TradeEvent.builder().id(21L).eventType(EventType.BUY).account(account).security(security).quantity(100L).build());
        service.processEvent(TradeEvent.builder().id(21L).eventType(EventType.CANCEL).account(account).security(security).quantity(0L).build());
        service.processEvent(TradeEvent.builder().id(22L).eventType(EventType.BUY).account(account).security(security).quantity(5L).build());
        long quantity = service.getPositionQuantity(Position.builder().account(account).security(security).build());
        int procesedEvents = PositionBookRepository.eventHistory.size();
        assertEquals("Expected: " + 5 + " amount of securities, got: " + quantity, 5, quantity);
        assertEquals("Expected: " + 2 + " processed events, got: " + procesedEvents, 2, procesedEvents);
    }

    @Test
    public void shouldProcessSellEvent () throws Exception {
        String account = "ACC1";
        String security = "SEC1";
        service.processEvent(TradeEvent.builder().id(7L).eventType(EventType.BUY).account(account).security(security).quantity(100L).build());
        service.processEvent(TradeEvent.builder().id(8L).eventType(EventType.SELL).account(account).security(security).quantity(50L).build());
        long quantity = service.getPositionQuantity(Position.builder().account(account).security(security).build());
        int procesedEvents = PositionBookRepository.eventHistory.size();
        assertEquals("Expected: " + 50 + " amount of securities, got: " + quantity, 50, quantity);
        assertEquals("Expected: " + 2 + " processed events, got: " + procesedEvents, 2, procesedEvents);
    }

    @Test(expected = Exception.class)
    public void shouldValidateEventForInsufficientSecurities () throws Exception {
        String account = "ACC1";
        String security = "SEC1";
        service.processEvent(TradeEvent.builder().id(9L).eventType(EventType.SELL).account(account).security(security).quantity(1000L).build());
        long quantity = service.getPositionQuantity(Position.builder().account(account).security(security).build());
        int procesedEvents = PositionBookRepository.eventHistory.size();
        assertEquals("Expected: " + 0 + " amount of securities, got: " + quantity, 0, quantity);
        assertEquals("Expected: " + 1 + " processed events, got: " + procesedEvents, 1, procesedEvents);
    }

    @Test
    public void shouldProcessEventsConcurrently() throws Exception {
        List<Long> buyEventIds = generateIds(1, 1000);
        List<Long> sellEventIds = generateIds(2001, 3000);
        List<Long> buyEventIds1 = generateIds(3001, 4000);
        List<Long> sellEventIds1 = generateIds(4001, 5000);
        List<Long> quantities = generateIds(1, 2000);

        Collection<TradeEvent> buyTradeEvents = generateExpectedEvents(buyEventIds, quantities, EventType.BUY, "ACC1", "SEC1");
        Collection<TradeEvent> sellTradeEvents = generateExpectedEvents(sellEventIds, quantities, EventType.SELL, "ACC1", "SEC1");

        Collection<TradeEvent> buyTradeEvents1 = generateExpectedEvents(buyEventIds1, quantities, EventType.BUY, "ACC1", "SEC2");
        Collection<TradeEvent> sellTradeEvents1 = generateExpectedEvents(sellEventIds1, quantities, EventType.SELL, "ACC1", "SEC2");

        //init for sufficient amount of securities
        long expectedQuantity = 100000000L;
        long expectedEvents = buyEventIds.size() + sellEventIds.size() + buyEventIds1.size() + sellEventIds1.size() + 1;
        service.processEvent(TradeEvent.builder().id(-1L).eventType(EventType.BUY).account("ACC1").security("SEC1").quantity(expectedQuantity).build());
        submitEventsConcurrently(buyTradeEvents, sellTradeEvents, buyTradeEvents1, sellTradeEvents1);

        int procesedEvents = PositionBookRepository.eventHistory.size();
        long actualQuantity = service.getPositionQuantity(Position.builder().account("ACC1").security("SEC1").build());
        List<Position> positions = service.getAllPositions();
        assertEquals("Expected: " + expectedQuantity + " amount of securities, got: " + actualQuantity, expectedQuantity, actualQuantity);
        assertEquals("Expected: " + expectedEvents + " processed events, got: " + procesedEvents, expectedEvents, procesedEvents);
        assertEquals("Expected: " + 2 + " positions, got: " + positions.size(), 2, positions.size());
    }

    private void submitEventsConcurrently(Collection<TradeEvent>... tradeEvents) throws InterruptedException {
        ScheduledThreadPoolExecutor threadPoolExecutor = new ScheduledThreadPoolExecutor(10);
        threadPoolExecutor.prestartAllCoreThreads();

        CountDownLatch readySteadyGo = new CountDownLatch(1);

        Arrays.stream(tradeEvents).forEach(events ->
                events.parallelStream().forEach(
                    event -> threadPoolExecutor.submit(
                                (Callable<Void>) () -> {
                                    readySteadyGo.await();
                                    service.processEvent(event);
                                    return null;
                                })
                    ));

        readySteadyGo.countDown();

        threadPoolExecutor.shutdown();
        threadPoolExecutor.awaitTermination(60L, TimeUnit.SECONDS);
    }

    private List<Long> generateIds(int startIndex, int size) {
        return LongStream.rangeClosed(startIndex, size)
                .boxed()
                .collect(toList());
    }

    private Collection<TradeEvent> generateExpectedEvents(Collection<Long> eventIds, Collection<Long> sequrityQuantity, EventType eventType, String account, String security) {
               return eventIds.stream()
                .flatMap(eventId -> sequrityQuantity.stream().map(quantity -> TradeEvent.builder()
                        .id(eventId)
                        .quantity(quantity)
                        .eventType(eventType)
                        .account(account)
                        .security(security)
                        .build()))
                .collect(Collectors.toList());
    }

}
