package com.arnur.transport.tools;

import com.arnur.transport.concurrent.ConcurrentArrivalSimulation;
import com.arnur.transport.integration.InspectionCoordinator;
import com.arnur.transport.observer.*;
import com.arnur.transport.observer.listener.*;
import com.arnur.transport.observer.source.*;
import com.arnur.transport.service.*;
import com.arnur.transport.visitor.operation.*;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

public final class SmokeTest {
    public static void main(String[] args) throws Exception {
        var network = SampleTransportTree.create();
        check(new BigDecimal("3700.00").equals(network.accept(new TicketRevenueVisitor())), "revenue");
        var audit = network.accept(new AccessibilityAuditVisitor());
        check(audit.checked() == 6 && audit.accessible() == 4, "accessibility audit");
        check(network.accept(new AccessibleElementFilterVisitor()).size() == 4, "filter");
        check(network.accept(new JsonExportVisitor()).contains("ticketMachine"), "json export");

        VehicleArrivalSource source = new VehicleArrivalSource(new ConcurrentEventSubject());
        DelayAlertService delays = new DelayAlertService(5, new ConcurrentEventSubject());
        ConcurrentEventSubject reports = new ConcurrentEventSubject();
        MaintenanceDashboardListener dashboard = new MaintenanceDashboardListener();
        source.subscribe(EventType.VEHICLE_ARRIVAL, delays);
        delays.subscribe(EventType.DELAY_REPORTED, new InspectionCoordinator(new TransportTreeIndex(network), reports));
        reports.subscribe(EventType.INSPECTION_REPORT, dashboard);
        source.arrival("B1", "R1", "S1", 7, 20);
        check(dashboard.reports().size() == 1, "observer-visitor integration");
        check("4".equals(dashboard.reports().get(0).attribute("checked")), "related subtree only");

        AtomicInteger count = new AtomicInteger();
        source.subscribe(EventType.VEHICLE_ARRIVAL, ignored -> count.incrementAndGet());
        int expected = new ConcurrentArrivalSimulation(source, 4).run(4, 1000);
        check(expected == 4000 && count.get() == 4000, "4x1000 stress count");

        RidershipSource ridership = new RidershipSource(new ConcurrentEventSubject());
        RidershipAggregator aggregator = new RidershipAggregator(new ConcurrentEventSubject());
        AtomicInteger aggregateEvents = new AtomicInteger();
        ridership.subscribe(EventType.RIDERSHIP_UPDATED, aggregator);
        aggregator.subscribe(EventType.AGGREGATED_METRIC, ignored -> aggregateEvents.incrementAndGet());
        ridership.record("R1", "S1", 10);
        ridership.record("R1", "S2", 5);
        check(aggregator.totalFor("R1") == 15 && aggregateEvents.get() == 2, "observer chain");

        System.out.println("SMOKE TEST PASSED: Observer, Visitor, integration, 4x1000 concurrency.");
    }

    private static void check(boolean condition, String name) {
        if (!condition) throw new AssertionError("Failed: " + name);
    }
}
