package com.arnur.transport.app;

import com.arnur.transport.concurrent.ConcurrentArrivalSimulation;
import com.arnur.transport.integration.InspectionCoordinator;
import com.arnur.transport.observer.*;
import com.arnur.transport.observer.listener.*;
import com.arnur.transport.observer.source.*;
import com.arnur.transport.service.*;
import com.arnur.transport.visitor.element.TransportNetwork;
import com.arnur.transport.visitor.operation.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class TransportApplication {
    public static void main(String[] args) throws Exception {
        int threadCount = args.length == 0 ? 3 : Integer.parseInt(args[0]);

        // Composition root: all objects are created and wired here through constructors.
        TransportNetwork network = SampleTransportTree.create();
        TransportTreeIndex index = new TransportTreeIndex(network);

        VehicleArrivalSource arrivals = new VehicleArrivalSource(new ConcurrentEventSubject());
        RidershipSource ridership = new RidershipSource(new ConcurrentEventSubject());
        DelayAlertService delayAlerts = new DelayAlertService(5, new ConcurrentEventSubject());
        RidershipAggregator aggregator = new RidershipAggregator(new ConcurrentEventSubject());
        ConcurrentEventSubject reportEvents = new ConcurrentEventSubject();

        StopDisplayListener display = new StopDisplayListener();
        PassengerAppListener passengerApp = new PassengerAppListener();
        JourneyCacheListener cache = new JourneyCacheListener();
        RidershipStatisticsListener statistics = new RidershipStatisticsListener();
        MaintenanceDashboardListener maintenance = new MaintenanceDashboardListener();
        OneShotArrivalListener oneShot = new OneShotArrivalListener(arrivals);
        InspectionCoordinator inspection = new InspectionCoordinator(index, reportEvents);

        arrivals.subscribe(EventType.VEHICLE_ARRIVAL, display);
        arrivals.subscribe(EventType.VEHICLE_ARRIVAL, passengerApp);
        arrivals.subscribe(EventType.VEHICLE_ARRIVAL, cache);
        arrivals.subscribe(EventType.VEHICLE_ARRIVAL, delayAlerts);
        arrivals.subscribe(EventType.VEHICLE_ARRIVAL, oneShot);
        ridership.subscribe(EventType.RIDERSHIP_UPDATED, statistics);
        ridership.subscribe(EventType.RIDERSHIP_UPDATED, aggregator);
        delayAlerts.subscribe(EventType.DELAY_REPORTED, passengerApp);
        delayAlerts.subscribe(EventType.DELAY_REPORTED, inspection);
        reportEvents.subscribe(EventType.INSPECTION_REPORT, maintenance);
        aggregator.subscribe(EventType.AGGREGATED_METRIC, passengerApp);

        arrivals.arrival("B1", "R1", "S1", 8, 32);
        arrivals.arrival("B2", "R2", "S3", 0, 18);
        ridership.record("R1", "S1", 32);
        ridership.record("R1", "S2", 14);

        System.out.println("=== OBSERVER ===");
        System.out.println("Stop S1 display: " + display.lastVehicleAt("S1"));
        System.out.println("One-shot listener count: " + oneShot.receivedCount());
        System.out.println("R1 aggregated boardings: " + aggregator.totalFor("R1"));
        System.out.println("Inspection reports: " + maintenance.reports().size());

        System.out.println("\n=== VISITOR ===");
        System.out.println("Ticket revenue: " + network.accept(new TicketRevenueVisitor()));
        var audit = network.accept(new AccessibilityAuditVisitor());
        System.out.println("Accessibility: " + audit.accessible() + "/" + audit.checked());
        System.out.println("Accessible elements: " + network.accept(new AccessibleElementFilterVisitor()).size());
        System.out.println("JSON: " + network.accept(new JsonExportVisitor()));

        AtomicInteger simulationCount = new AtomicInteger();
        arrivals.subscribe(EventType.VEHICLE_ARRIVAL, ignored -> simulationCount.incrementAndGet());
        int producers = Math.max(3, threadCount);
        int expected = new ConcurrentArrivalSimulation(arrivals, threadCount).run(producers, 20);
        System.out.println("\n=== CONCURRENCY ===");
        System.out.println("Producer threads: " + producers + ", pool size: " + threadCount);
        System.out.println("Expected events: " + expected + ", observed: " + simulationCount.get());
    }
}
