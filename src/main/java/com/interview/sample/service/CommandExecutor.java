package com.interview.sample.service;

import com.interview.sample.controller.command.Command;
import com.interview.sample.controller.command.InstrumentCreatedCommand;
import com.interview.sample.controller.command.OrderCancelledCommand;
import com.interview.sample.controller.command.OrderPlacedCommand;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * The CommandExecutor class is responsible for executing commands in a thread-safe and ordered manner.
 * It ensures that commands related to the same instrument and OrderBook are executed sequentially
 * using a single thread, while limiting the total number of threads to a maximum of {@code MAX_THREADS}.

 * <p>Implementation Details:
 * <ul>
 *     <li>The {@code workers} list contains a fixed number of single-threaded executors.</li>
 *     <li>The {@code instrumentWorkers} map assigns each instrument to one of the available threads.</li>
 *     <li>The {@code counter} ensures a round-robin assignment of threads to instruments.</li>
 * </ul>
 *
 * <p>Thread Safety:
 * <ul>
 *     <li>Commands for the same instrument are executed sequentially using the same thread.</li>
 *     <li>Commands for different instruments may be executed concurrently on different threads.</li>
 * </ul>
 */
@SuppressWarnings("unchecked")
@Component
@RequiredArgsConstructor
public class CommandExecutor {
    private static final int MAX_THREADS = 10;

    private final InstrumentCreatedCommandHandler instrumentCreatedCommandHandler;
    private final OrderCancelledCommandHandler orderCancelledCommandHandler;
    private final OrderPlacedCommandHandler orderPlacedCommandHandler;


    private final Map<Class<? extends Command>, CommandHandler<?>> handlers = new HashMap<>();
    private final List<ExecutorService> workers = new ArrayList<>(MAX_THREADS);
    private final ConcurrentMap<String, ExecutorService> instrumentWorkers = new ConcurrentHashMap<>();
    private final AtomicInteger counter = new AtomicInteger();

    @PostConstruct
    public void init() {
        handlers.put(InstrumentCreatedCommand.class, instrumentCreatedCommandHandler);
        handlers.put(OrderCancelledCommand.class, orderCancelledCommandHandler);
        handlers.put(OrderPlacedCommand.class, orderPlacedCommandHandler);

        for (int i = 0; i < MAX_THREADS; i++) {
            workers.add(Executors.newSingleThreadExecutor());
        }
    }

    public <T extends Command> void execute(T command) {
        String instrumentId = extractInstrumentId(command);

        ExecutorService worker = instrumentWorkers.computeIfAbsent(instrumentId,
                id -> workers.get(counter.getAndIncrement() % MAX_THREADS));

        worker.execute(() -> {
            CommandHandler<T> handler = (CommandHandler<T>) handlers.get(command.getClass());
            if (handler != null) {
                handler.handle(command);
            } else {
                throw new IllegalStateException("No handler for: " + command.getClass());
            }
        });
    }

    private String extractInstrumentId(Command command) {
        return switch (command) {
            case InstrumentCreatedCommand cmd -> cmd.securityId();
            case OrderCancelledCommand cmd -> cmd.securityId();
            case OrderPlacedCommand cmd -> cmd.securityId();
            default -> throw new IllegalArgumentException("Unknown command type");
        };
    }

    @PreDestroy
    public void shutdown() {
        workers.forEach(executor -> {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }
}