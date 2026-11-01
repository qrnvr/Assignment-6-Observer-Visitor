package com.arnur.transport.observer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ConcurrentEventSubject implements Subject<TransportEvent> {
    private final Map<EventType, CopyOnWriteArrayList<Observer<TransportEvent>>> registry = new ConcurrentHashMap<>();

    @Override
    public void subscribe(EventType type, Observer<TransportEvent> observer) {
        registry.computeIfAbsent(type, ignored -> new CopyOnWriteArrayList<>()).addIfAbsent(observer);
    }

    @Override
    public void unsubscribe(EventType type, Observer<TransportEvent> observer) {
        List<Observer<TransportEvent>> listeners = registry.get(type);
        if (listeners != null) {
            listeners.remove(observer);
        }
    }

    @Override
    public void publish(TransportEvent event) {
        List<Observer<TransportEvent>> listeners = registry.get(event.type());
        if (listeners == null) {
            return;
        }
        for (Observer<TransportEvent> observer : listeners) {
            observer.onEvent(event);
        }
    }

    public int listenerCount(EventType type) {
        List<Observer<TransportEvent>> listeners = registry.get(type);
        return listeners == null ? 0 : listeners.size();
    }
}
