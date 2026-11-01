package com.arnur.transport.observer.listener;

import com.arnur.transport.observer.Observer;
import com.arnur.transport.observer.TransportEvent;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class MaintenanceDashboardListener implements Observer<TransportEvent> {
    private final ConcurrentLinkedQueue<TransportEvent> reports = new ConcurrentLinkedQueue<>();

    @Override
    public void onEvent(TransportEvent event) {
        reports.add(event);
    }

    public List<TransportEvent> reports() { return List.copyOf(reports); }
}
