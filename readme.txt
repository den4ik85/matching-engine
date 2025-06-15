Matching Engine for Financial Trading
The matching engine is designed to process and manage orders for instruments in a trading environment.
It provides functionality for order placement, cancellation, instrument management, event handling, and order matching.
The system ensures thread-safe execution of commands and validates them using dedicated validators.

<hr></hr>
Key Features

1. As an entry points we InstrumentController and OrderController which is in real life represented
as high-performance IPC like Aeron or TCP/UDP with Protocol Buffers, FPGA or Confinity LLM

2. Instrument Creation
Command: InstrumentCreatedCommand
Handler: InstrumentCreatedCommandHandler
Description: Creates instruments and initializes their order books.
Flow:
 - Validate the command using InstrumentCreatedCommandValidator.
 - Check if the instrument already exists in BookRepository.
 - Create a new Instrument and its OrderBook using OrderBookFactory.
 - Add the OrderBook to BookRepository.

3. Order Placement
Command: OrderPlacedCommand
Handler: OrderPlacedCommandHandler
Description: Accepts and validates buy/sell orders. Matches them against existing orders in the order book using the auction strategy (PriceTimeMatcher).
Flow:
 - Send the command using OrderControllerTest.
 - Retrieve the corresponding OrderBook from BookRepository.
 - Transform the command into an Order using OrderCommandTransformer.
 - Place the order in the OrderBook.
 - Publish events (TradeEvent, OrderRejectedEvent) based on the matching results.

4. Order Cancellation
Command: OrderCancelledCommand
Handler: OrderCancelledCommandHandler
Description: Validates and cancels orders in the order book.
Flow:
 - Validate the command using OrderCancelledCommandValidator.
 - Retrieve the corresponding OrderBook from BookRepository.
 - Cancel the order in the OrderBook.
 - Publish events (OrderCancelledEvent, OrderCancelRejectedEvent) based on the cancellation results.

5. Event Handling
Component: EventBroker
In real matching engines can be used low-latency messaging tools UDP Multicast like Confinity LLM /Aeron
or high-performance message brokers like Kafka for inter component communication.
Description: Publishes events to notify other components about order execution, cancellation, or rejection.
Flow:
 - Events are published to a queue.
 - A consumer processes events asynchronously in FIFO order.
 - Events are passed to the subscribed components.

6. Order Matching
Strategy: PriceTimeMatcher can be enhanced with other custom matching strategies and OrderType with TimeInForce
Now it supports following combinations:
 - OrderType.LIMIT with TimeInForce.ALL_OR_NONE
 - OrderType.MARKET with TimeInForce.FILL_OR_KILL
Description: Matches orders based on price and time priority.
Flow:
 - Validate the TimeInForce and OrderType of the incoming order.
 - Match the incoming order against the opposing side of the order book.
 - Generate TradeEvent for successful matches.
 - Update order quantities and remove fully filled orders from the book.

<hr></hr>
System Architecture
Command Execution / Transaction Context
Component: CommandExecutor
Description: Ensures thread-safe execution of commands using a pool of single-threaded executors for each OrderBook.
In real world matching engines, this can be replaced with a more sophisticated thread management system. For this usually
used ring buffers of disruptor to guarantee transaction context access to the order book per instrument:
 - Strict Thread Confinement: Each book has exactly 1 thread
 - Bounded Thread Pool: e.g., 1 thread per core
 - Wait-Free Publishing: Ring buffers for incoming orders
 - Hot Book Optimization: Frequently traded books get dedicated cores
Flow:
 - Commands are assigned to threads based on their instrument ID.
 - Commands for the same instrument are executed sequentially.
 - Commands for different instruments may be executed concurrently.


<hr></hr>
Main Asynchronous Flows
1. Command Execution
Commands are submitted to the CommandExecutor.
The executor assigns commands to threads based on their instrument ID.
Commands are processed sequentially for the same instrument.
2. Event Processing
Events are published to the EventBroker.
The broker processes events asynchronously in FIFO order.
Events are passed to subscribed components for further handling.

<hr></hr>
Dependencies
Spring Boot: Provides dependency injection and REST API support.
Mockito: Used for unit testing and mocking dependencies.
Lombok: Reduces boilerplate code for models and components.

<hr></hr>
How to Run
Build the project using Maven and jdk 21:
mvn clean install
Run end2end integration tests in MatchingEngineIntegrationTest class to verify the functionality of the matching engine.
MatchingEngineIntegrationTest contains multithreaded tests that simulate concurrent order placements and cancellations.
Use the REST API endpoints to interact with the system:
/orders/submit: Submit a new order.
/orders/cancel: Cancel an existing order.
/instruments: Create a new instrument.

<hr></hr>
Future Enhancements
Order status to handle
 - EXPIRED (for DAY orders of GOOD_TILL_DATE orders),
 - SUSPENDED (when we need to pause for example during Circuit Breaker for wash trade prevention or price corridor),
Support for additional order types and TimeInForce values (DAY, GOOD_TILL_CANCEL, IMMEDIATE_OR_CANCEL, GOOD_TILL_DATE)
Integration with external systems for event consumption.