package com.arnur.transport.observer.listener;

import com.arnur.transport.observer.Observer;
import com.arnur.transport.observer.TransportEvent;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class JourneyCacheListener implements Observer<TransportEvent> {
    private final Map<String, TransportEvent> latestByVehicle = new ConcurrentHashMap<>();

    @Override
    public void onEvent(TransportEvent event) {
        String vehicleId = event.attribute("vehicleId");
        if (vehicleId != null) {
            latestByVehicle.put(vehicleId, event);
        }
    }

    public TransportEvent latestFor(String vehicleId) {
        return latestByVehicle.get(vehicleId);
    }
}
