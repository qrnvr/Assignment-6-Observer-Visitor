package com.arnur.transport.observer.listener;

import com.arnur.transport.observer.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class OneShotArrivalListener implements Observer<TransportEvent> {
    private final Subject<TransportEvent> subject;
    private final AtomicInteger received = new AtomicInteger();

    public OneShotArrivalListener(Subject<TransportEvent> subject) {
        this.subject = subject;
    }

    @Override
    public void onEvent(TransportEvent event) {
        received.incrementAndGet();
        subject.unsubscribe(EventType.VEHICLE_ARRIVAL, this);
    }

    public int receivedCount() { return received.get(); }
}
