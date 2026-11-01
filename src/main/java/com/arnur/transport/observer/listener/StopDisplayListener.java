package com.arnur.transport.observer.listener;

import com.arnur.transport.observer.Observer;
import com.arnur.transport.observer.TransportEvent;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class StopDisplayListener implements Observer<TransportEvent> {
    private final Map<String, String> lastVehicleByStop = new ConcurrentHashMap<>();

    @Override
    public void onEvent(TransportEvent event) {
        String stopId = event.attribute("stopId");
        String vehicleId = event.attribute("vehicleId");
        if (stopId != null && vehicleId != null) {
            lastVehicleByStop.put(stopId, vehicleId);
        }
    }

    public String lastVehicleAt(String stopId) {
        return lastVehicleByStop.get(stopId);
    }
}
