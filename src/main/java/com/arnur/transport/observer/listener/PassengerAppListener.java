package com.arnur.transport.observer.listener;

import com.arnur.transport.observer.Observer;
import com.arnur.transport.observer.TransportEvent;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class PassengerAppListener implements Observer<TransportEvent> {
    private final ConcurrentLinkedQueue<String> messages = new ConcurrentLinkedQueue<>();

    @Override
    public void onEvent(TransportEvent event) {
        messages.add(event.type() + " route=" + event.routeId());
    }

    public List<String> messages() {
        return List.copyOf(messages);
    }
}
