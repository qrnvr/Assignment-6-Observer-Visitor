package com.arnur.transport.observer.source;

import com.arnur.transport.observer.*;
import java.time.Instant;
import java.util.Map;

public final class RidershipSource implements Subject<TransportEvent> {
    private final ConcurrentEventSubject events;

    public RidershipSource(ConcurrentEventSubject events) {
        this.events = events;
    }

    public void record(String routeId, String stopId, int boardings) {
        events.publish(new TransportEvent(
                EventType.RIDERSHIP_UPDATED,
                "ridership-source",
                routeId,
                Instant.now(),
                Map.of("stopId", stopId, "boardings", Integer.toString(boardings))));
    }

    @Override public void subscribe(EventType type, Observer<TransportEvent> observer) { events.subscribe(type, observer); }
    @Override public void unsubscribe(EventType type, Observer<TransportEvent> observer) { events.unsubscribe(type, observer); }
    @Override public void publish(TransportEvent event) { events.publish(event); }
}
