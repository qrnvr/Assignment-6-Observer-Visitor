package com.arnur.transport.observer.listener;

import com.arnur.transport.observer.*;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

public final class RidershipAggregator implements Observer<TransportEvent>, Subject<TransportEvent> {
    private final Map<String, LongAdder> totalsByRoute = new ConcurrentHashMap<>();
    private final ConcurrentEventSubject aggregatedEvents;

    public RidershipAggregator(ConcurrentEventSubject aggregatedEvents) {
        this.aggregatedEvents = aggregatedEvents;
    }

    @Override
    public void onEvent(TransportEvent event) {
        LongAdder total = totalsByRoute.computeIfAbsent(event.routeId(), ignored -> new LongAdder());
        total.add(event.intAttribute("boardings"));
        aggregatedEvents.publish(new TransportEvent(
                EventType.AGGREGATED_METRIC,
                "ridership-aggregator",
                event.routeId(),
                Instant.now(),
                Map.of("totalBoardings", Long.toString(total.sum()))));
    }

    public long totalFor(String routeId) {
        LongAdder value = totalsByRoute.get(routeId);
        return value == null ? 0 : value.sum();
    }

    @Override public void subscribe(EventType type, Observer<TransportEvent> observer) { aggregatedEvents.subscribe(type, observer); }
    @Override public void unsubscribe(EventType type, Observer<TransportEvent> observer) { aggregatedEvents.unsubscribe(type, observer); }
    @Override public void publish(TransportEvent event) { aggregatedEvents.publish(event); }
}
