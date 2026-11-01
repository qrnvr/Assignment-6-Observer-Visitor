package com.arnur.transport.observer.source;

import com.arnur.transport.observer.*;
import java.time.Instant;
import java.util.Map;

public final class VehicleArrivalSource implements Subject<TransportEvent> {
    private final ConcurrentEventSubject events;

    public VehicleArrivalSource(ConcurrentEventSubject events) {
        this.events = events;
    }

    public void arrival(String vehicleId, String routeId, String stopId, int delayMinutes, int passengers) {
        events.publish(new TransportEvent(
                EventType.VEHICLE_ARRIVAL,
                vehicleId,
                routeId,
                Instant.now(),
                Map.of(
                        "vehicleId", vehicleId,
                        "stopId", stopId,
                        "delayMinutes", Integer.toString(delayMinutes),
                        "passengers", Integer.toString(passengers))));
    }

    @Override public void subscribe(EventType type, Observer<TransportEvent> observer) { events.subscribe(type, observer); }
    @Override public void unsubscribe(EventType type, Observer<TransportEvent> observer) { events.unsubscribe(type, observer); }
    @Override public void publish(TransportEvent event) { events.publish(event); }
}
