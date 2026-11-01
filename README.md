# Assignment 6 - Observer and Visitor Patterns

**Student:** Arnur Sadyrov  
**Group:** SE-2532  
**Variant:** 15 - Public Transport  
**Repository:** https://github.com/qrnvr/Assignment-6-Observer-Visitor

## Goal

The project models a public-transport system with two behavioral patterns:

- **Observer** handles live vehicle-arrival and ridership events.
- **Visitor** adds operations to a stable transport-tree structure.
- **Integration** connects them: a delayed arrival creates a delay event, which runs a Visitor only on the affected route and publishes an inspection report.

## Run

Requirements: Java 17+. Maven is only needed for JUnit.

```bash
./run.sh
```

Optional pool size:

```bash
./run.sh 4
```

Dependency-free verification:

```bash
./smoke-test.sh
```

JUnit 5 tests:

```bash
mvn test
```

## Observer design

### Sources

1. `VehicleArrivalSource` publishes `VEHICLE_ARRIVAL`.
2. `RidershipSource` publishes `RIDERSHIP_UPDATED`.

Both use the custom generic `Subject<E>` / `Observer<E>` contracts and a thread-safe `ConcurrentEventSubject` registry.

### Listeners

- `StopDisplayListener` - remembers the latest vehicle for each stop.
- `PassengerAppListener` - collects passenger-facing notifications.
- `JourneyCacheListener` - caches the latest event per vehicle.
- `RidershipStatisticsListener` - counts ridership events and boardings.
- `DelayAlertService` - listens for arrivals and republishes `DELAY_REPORTED` events.
- `RidershipAggregator` - listens for ridership updates and republishes cumulative `AGGREGATED_METRIC` events.
- `OneShotArrivalListener` - unsubscribes itself from inside its own handler.
- `MaintenanceDashboardListener` - receives inspection reports.

`RidershipAggregator` demonstrates the required **observer chain** because it is both an Observer and a Subject and republishes aggregated route totals.

### Push vs. pull

This solution uses **push**. Each immutable `TransportEvent` carries the data a listener needs. Listeners do not query mutable source state after notification. This is simple for concurrent publishers and keeps event handling deterministic.

### Runtime subscription

The registry uses `ConcurrentHashMap` plus `CopyOnWriteArrayList`. Listeners may subscribe or unsubscribe while events are being published. No monitor locks the whole subject. `OneShotArrivalListener` proves that a listener may unsubscribe from inside `onEvent`.

## Visitor design

### Tree

The final tree has four levels:

```text
TransportNetwork
  -> Route
      -> Stop
          -> Bus
          -> Tram
          -> StationFacility
          -> TicketMachine
```

There are three container types (`TransportNetwork`, `Route`, `Stop`) and four leaf types.

### Generic visitor

`TransportVisitor<R>` defines one visit method for every concrete element. Each element implements `accept(visitor)` and calls its matching visit method. This is double dispatch.

### Visitors

- `TicketRevenueVisitor` - numeric calculation.
- `AccessibilityAuditVisitor` - accumulates accessibility totals and issue IDs over the whole subtree.
- `AccessibleElementFilterVisitor` - returns a list of accessible leaf elements.
- `JsonExportVisitor` - hand-written nested JSON export.

### Traversal decision

**The visitor owns traversal.** Container elements only hold immutable data and dispatch to the visitor. Visitors recursively decide whether to continue into children. This keeps traversal policy with the operation and allows integration code to start at a single `Route` instead of always traversing the complete network.

## Observer + Visitor integration

Flow:

```text
VehicleArrivalSource
  -> DelayAlertService
      -> DELAY_REPORTED
          -> InspectionCoordinator
              -> TransportTreeIndex finds only the related Route
                  -> AccessibilityAuditVisitor runs on that Route
                      -> INSPECTION_REPORT
                          -> MaintenanceDashboardListener
```

A delay on `R1` inspects `R1` only. It does not visit `R2`.

## Concurrency

`ConcurrentArrivalSimulation` uses an `ExecutorService` with a configurable pool size and at least three producers. Shared listener state uses concurrent collections, atomics, or `LongAdder`. The registry does not synchronize the entire subject.

Shutdown sequence:

1. submit producer tasks;
2. release them together;
3. call `shutdown()`;
4. wait for every `Future`;
5. await executor termination;
6. use `shutdownNow()` only as a fallback.

The stress test publishes **4 x 1,000 = 4,000 events** and asserts exactly 4,000 deliveries.

## Clean Code decisions

- `TransportApplication` is the single production composition root.
- Dependencies are passed through constructors.
- Business objects are created eagerly; there is no lazy initialization in business logic.
- Element records contain immutable data plus Visitor dispatch only.
- Event objects are immutable records with defensive copies of attributes.
- Repeated event-registry logic is centralized in `ConcurrentEventSubject`.

## Tests

There are **20 JUnit 5 test methods** covering:

- event-type subscriptions;
- runtime unsubscribe;
- unsubscribe inside a handler;
- immutable events;
- delay republishing;
- ridership totals;
- aggregated observer chain;
- ticket revenue using hand-calculated values;
- accessibility totals using hand-calculated values;
- filter results;
- nested JSON;
- double dispatch;
- Observer -> Visitor integration on one route;
- unknown-route handling;
- exact 4-thread x 1,000-event stress count;
- configurable ExecutorService behavior.

Hand-calculated final-tree values:

- total ticket revenue = **3700.00**;
- route R1 revenue = **2500.00**;
- accessibility = **4 accessible / 6 checked**;
- accessible-element filter size = **4**.

## Visitor trade-off proof

### New visitor commit

Commit: [`914d474b74f593fdd5cdf426d0e2924e805b9aa5`](https://github.com/qrnvr/Assignment-6-Observer-Visitor/commit/914d474b74f593fdd5cdf426d0e2924e805b9aa5)

Changed files:

```text
src/main/java/com/arnur/transport/visitor/operation/JsonExportVisitor.java
```

No element class changed. This demonstrates Visitor's strength: adding a new operation is cheap when element types are stable.

### New element commit

Commit: [`de8e1d5202746050209060b52665692fb7fd8ea2`](https://github.com/qrnvr/Assignment-6-Observer-Visitor/commit/de8e1d5202746050209060b52665692fb7fd8ea2)

Changed files:

```text
src/main/java/com/arnur/transport/service/SampleTransportTree.java
src/main/java/com/arnur/transport/visitor/TransportVisitor.java
src/main/java/com/arnur/transport/visitor/element/TicketMachine.java
src/main/java/com/arnur/transport/visitor/operation/AccessibilityAuditVisitor.java
src/main/java/com/arnur/transport/visitor/operation/AccessibleElementFilterVisitor.java
src/main/java/com/arnur/transport/visitor/operation/JsonExportVisitor.java
src/main/java/com/arnur/transport/visitor/operation/TicketRevenueVisitor.java
```

Adding an element is more expensive because the visitor contract and every existing visitor must understand the new element. If element types changed frequently while operations were stable, classic Visitor would be a poor fit.

## UML

See [`docs/UML.png`](docs/UML.png).

## Important defense points

- Observer solves one-to-many event notification without hard-coding listeners into sources.
- Visitor solves adding operations to a stable set of element types.
- `accept()` belongs to the element; `visitX()` belongs to the visitor.
- The listener registry remains safe while subscriptions change during concurrent publishing.
- The integration intentionally runs the visitor on the **related route subtree only**.
