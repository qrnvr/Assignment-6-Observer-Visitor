package com.arnur.transport;

import com.arnur.transport.observer.*;
import com.arnur.transport.observer.listener.*;
import com.arnur.transport.observer.source.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ObserverPatternTest {
    @Test void matchingSubscribersOnlyReceiveTheirType() {
        ConcurrentEventSubject subject = new ConcurrentEventSubject();
        AtomicInteger arrivals = new AtomicInteger();
        AtomicInteger reports = new AtomicInteger();
        subject.subscribe(EventType.VEHICLE_ARRIVAL, e -> arrivals.incrementAndGet());
        subject.subscribe(EventType.INSPECTION_REPORT, e -> reports.incrementAndGet());
        subject.publish(event(EventType.VEHICLE_ARRIVAL, "R1", Map.of()));
        assertEquals(1, arrivals.get());
        assertEquals(0, reports.get());
    }

    @Test void unsubscribeStopsDelivery() {
        ConcurrentEventSubject subject = new ConcurrentEventSubject();
        AtomicInteger count = new AtomicInteger();
        Observer<TransportEvent> observer = e -> count.incrementAndGet();
        subject.subscribe(EventType.VEHICLE_ARRIVAL, observer);
        subject.publish(event(EventType.VEHICLE_ARRIVAL, "R1", Map.of()));
        subject.unsubscribe(EventType.VEHICLE_ARRIVAL, observer);
        subject.publish(event(EventType.VEHICLE_ARRIVAL, "R1", Map.of()));
        assertEquals(1, count.get());
    }

    @Test void selfUnsubscribeWorksInsideHandler() {
        VehicleArrivalSource source = new VehicleArrivalSource(new ConcurrentEventSubject());
        OneShotArrivalListener listener = new OneShotArrivalListener(source);
        source.subscribe(EventType.VEHICLE_ARRIVAL, listener);
        source.arrival("B1", "R1", "S1", 0, 10);
        source.arrival("B1", "R1", "S2", 0, 11);
        assertEquals(1, listener.receivedCount());
    }

    @Test void eventAttributesAreImmutable() {
        TransportEvent event = event(EventType.VEHICLE_ARRIVAL, "R1", Map.of("x", "1"));
        assertThrows(UnsupportedOperationException.class, () -> event.attributes().put("x", "2"));
    }

    @Test void delayServiceRepublishesAtThreshold() {
        VehicleArrivalSource source = new VehicleArrivalSource(new ConcurrentEventSubject());
        DelayAlertService delay = new DelayAlertService(5, new ConcurrentEventSubject());
        AtomicReference<TransportEvent> seen = new AtomicReference<>();
        source.subscribe(EventType.VEHICLE_ARRIVAL, delay);
        delay.subscribe(EventType.DELAY_REPORTED, seen::set);
        source.arrival("B1", "R1", "S1", 5, 20);
        assertNotNull(seen.get());
        assertEquals("R1", seen.get().routeId());
    }

    @Test void delayServiceIgnoresBelowThreshold() {
        VehicleArrivalSource source = new VehicleArrivalSource(new ConcurrentEventSubject());
        DelayAlertService delay = new DelayAlertService(5, new ConcurrentEventSubject());
        AtomicInteger seen = new AtomicInteger();
        source.subscribe(EventType.VEHICLE_ARRIVAL, delay);
        delay.subscribe(EventType.DELAY_REPORTED, e -> seen.incrementAndGet());
        source.arrival("B1", "R1", "S1", 4, 20);
        assertEquals(0, seen.get());
    }

    @Test void ridershipStatisticsSumsBoardings() {
        RidershipSource source = new RidershipSource(new ConcurrentEventSubject());
        RidershipStatisticsListener statistics = new RidershipStatisticsListener();
        source.subscribe(EventType.RIDERSHIP_UPDATED, statistics);
        source.record("R1", "S1", 10);
        source.record("R1", "S2", 15);
        assertEquals(25, statistics.totalBoardings());
        assertEquals(2, statistics.eventCount());
    }

    @Test void aggregatorRepublishesCumulativeTotal() {
        RidershipSource source = new RidershipSource(new ConcurrentEventSubject());
        RidershipAggregator aggregator = new RidershipAggregator(new ConcurrentEventSubject());
        AtomicReference<TransportEvent> latest = new AtomicReference<>();
        source.subscribe(EventType.RIDERSHIP_UPDATED, aggregator);
        aggregator.subscribe(EventType.AGGREGATED_METRIC, latest::set);
        source.record("R1", "S1", 10);
        source.record("R1", "S2", 7);
        assertEquals(17, aggregator.totalFor("R1"));
        assertEquals("17", latest.get().attribute("totalBoardings"));
    }

    @Test void runtimeSubscriptionReceivesOnlyLaterEvents() {
        VehicleArrivalSource source = new VehicleArrivalSource(new ConcurrentEventSubject());
        AtomicInteger count = new AtomicInteger();
        source.arrival("B1", "R1", "S1", 0, 1);
        source.subscribe(EventType.VEHICLE_ARRIVAL, e -> count.incrementAndGet());
        source.arrival("B2", "R1", "S1", 0, 1);
        assertEquals(1, count.get());
    }

    private TransportEvent event(EventType type, String routeId, Map<String, String> attributes) {
        return new TransportEvent(type, "test", routeId, Instant.EPOCH, attributes);
    }
}
