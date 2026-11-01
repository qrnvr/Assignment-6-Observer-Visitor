package com.arnur.transport.observer.listener;

import com.arnur.transport.observer.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public final class DelayAlertService implements Observer<TransportEvent>, Subject<TransportEvent> {
    private final int thresholdMinutes;
    private final ConcurrentEventSubject republishedEvents;

    public DelayAlertService(int thresholdMinutes, ConcurrentEventSubject republishedEvents) {
        this.thresholdMinutes = thresholdMinutes;
        this.republishedEvents = republishedEvents;
    }

    @Override
    public void onEvent(TransportEvent event) {
        int delay = event.intAttribute("delayMinutes");
        if (delay < thresholdMinutes) {
            return;
        }
        Map<String, String> attributes = new HashMap<>(event.attributes());
        attributes.put("alert", "delay-threshold-exceeded");
        republishedEvents.publish(new TransportEvent(
                EventType.DELAY_REPORTED,
                "delay-alert-service",
                event.routeId(),
                Instant.now(),
                attributes));
    }

    @Override public void subscribe(EventType type, Observer<TransportEvent> observer) { republishedEvents.subscribe(type, observer); }
    @Override public void unsubscribe(EventType type, Observer<TransportEvent> observer) { republishedEvents.unsubscribe(type, observer); }
    @Override public void publish(TransportEvent event) { republishedEvents.publish(event); }
}
