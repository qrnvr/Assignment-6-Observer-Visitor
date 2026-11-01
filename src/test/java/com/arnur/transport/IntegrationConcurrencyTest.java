package com.arnur.transport;

import com.arnur.transport.concurrent.ConcurrentArrivalSimulation;
import com.arnur.transport.integration.InspectionCoordinator;
import com.arnur.transport.observer.*;
import com.arnur.transport.observer.listener.*;
import com.arnur.transport.observer.source.VehicleArrivalSource;
import com.arnur.transport.service.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class IntegrationConcurrencyTest {
    @Test void delayTriggersVisitorOnRelatedRouteOnly() {
        var network = SampleTransportTree.create();
        VehicleArrivalSource source = new VehicleArrivalSource(new ConcurrentEventSubject());
        DelayAlertService delay = new DelayAlertService(5, new ConcurrentEventSubject());
        ConcurrentEventSubject reports = new ConcurrentEventSubject();
        MaintenanceDashboardListener dashboard = new MaintenanceDashboardListener();
        InspectionCoordinator coordinator = new InspectionCoordinator(new TransportTreeIndex(network), reports);
        source.subscribe(EventType.VEHICLE_ARRIVAL, delay);
        delay.subscribe(EventType.DELAY_REPORTED, coordinator);
        reports.subscribe(EventType.INSPECTION_REPORT, dashboard);

        source.arrival("B1", "R1", "S1", 9, 20);

        assertEquals(1, dashboard.reports().size());
        TransportEvent report = dashboard.reports().get(0);
        assertEquals("R1", report.routeId());
        assertEquals("4", report.attribute("checked"));
        assertEquals("2", report.attribute("accessible"));
        assertEquals("2", report.attribute("issues"));
    }

    @Test void unknownRouteProducesNoInspectionReport() {
        ConcurrentEventSubject reports = new ConcurrentEventSubject();
        MaintenanceDashboardListener dashboard = new MaintenanceDashboardListener();
        reports.subscribe(EventType.INSPECTION_REPORT, dashboard);
        InspectionCoordinator coordinator = new InspectionCoordinator(new TransportTreeIndex(SampleTransportTree.create()), reports);
        coordinator.onEvent(new TransportEvent(EventType.DELAY_REPORTED, "test", "UNKNOWN", Instant.EPOCH, Map.of()));
        assertTrue(dashboard.reports().isEmpty());
    }

    @Test void stressTestFourThreadsThousandEventsExactCount() throws Exception {
        VehicleArrivalSource source = new VehicleArrivalSource(new ConcurrentEventSubject());
        AtomicInteger count = new AtomicInteger();
        source.subscribe(EventType.VEHICLE_ARRIVAL, event -> count.incrementAndGet());
        int expected = new ConcurrentArrivalSimulation(source, 4).run(4, 1000);
        assertEquals(4000, expected);
        assertEquals(4000, count.get());
    }

    @Test void configurablePoolWithThreeProducerThreadsLosesNoEvents() throws Exception {
        VehicleArrivalSource source = new VehicleArrivalSource(new ConcurrentEventSubject());
        AtomicInteger count = new AtomicInteger();
        source.subscribe(EventType.VEHICLE_ARRIVAL, event -> count.incrementAndGet());
        int expected = new ConcurrentArrivalSimulation(source, 2).run(3, 200);
        assertEquals(600, expected);
        assertEquals(600, count.get());
    }
}
