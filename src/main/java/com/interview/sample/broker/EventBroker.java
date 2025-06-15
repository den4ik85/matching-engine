package com.interview.sample.broker;

import com.interview.sample.domain.event.Event;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@Log4j2
@Component
public class EventBroker {

    private final BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final AtomicReference<Consumer<Event>> eventConsumer = new AtomicReference<>();

    // Publish an event to the queue
    public void publish(Event event) {
        try {
            eventQueue.put(event);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Failed to publish event", e);
            throw new IllegalStateException("Failed to publish event", e);
        }
    }

    // Subscribe to process events
    public void subscribe(Consumer<Event> consumer) {
        eventConsumer.set(consumer);
        executorService.submit(this::processEvents); // Start processing events
    }

    // Process events in FIFO order
    private void processEvents() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Event event = eventQueue.take(); // Retrieve and remove the head of the queue
                Consumer<Event> consumer = eventConsumer.get();
                if (consumer != null) {
                    consumer.accept(event); // Pass the event to the consumer
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Event processing interrupted", e);
                break; // Exit the loop if interrupted
            } catch (Exception e) {
                log.error("Error while processing event", e);
            }
        }
    }

    // Clear the event queue (optional, for cleanup)
    public void clear() {
        eventQueue.clear();
        eventConsumer.set(null);
    }

    // Shutdown the event broker gracefully
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(1, TimeUnit.MINUTES)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executorService.shutdownNow();
        }
    }
}