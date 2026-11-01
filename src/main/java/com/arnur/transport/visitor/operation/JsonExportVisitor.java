package com.arnur.transport.visitor.operation;

import com.arnur.transport.visitor.*;
import com.arnur.transport.visitor.element.*;
import java.util.stream.Collectors;

public final class JsonExportVisitor implements TransportVisitor<String> {
    @Override public String visitNetwork(TransportNetwork network) {
        return "{\"type\":\"network\",\"id\":\"" + esc(network.id()) + "\",\"name\":\"" + esc(network.name()) +
                "\",\"routes\":[" + network.routes().stream().map(route -> route.accept(this)).collect(Collectors.joining(",")) + "]}";
    }
    @Override public String visitRoute(Route route) {
        return "{\"type\":\"route\",\"id\":\"" + esc(route.id()) + "\",\"name\":\"" + esc(route.name()) +
                "\",\"stops\":[" + route.stops().stream().map(stop -> stop.accept(this)).collect(Collectors.joining(",")) + "]}";
    }
    @Override public String visitStop(Stop stop) {
        return "{\"type\":\"stop\",\"id\":\"" + esc(stop.id()) + "\",\"name\":\"" + esc(stop.name()) +
                "\",\"elements\":[" + stop.elements().stream().map(element -> element.accept(this)).collect(Collectors.joining(",")) + "]}";
    }
    @Override public String visitBus(Bus bus) { return leaf("bus", bus.id(), bus.name(), bus.accessible()); }
    @Override public String visitTram(Tram tram) { return leaf("tram", tram.id(), tram.name(), tram.accessible()); }
    @Override public String visitStationFacility(StationFacility station) { return leaf("station", station.id(), station.name(), station.accessible()); }

    private String leaf(String type, String id, String name, boolean accessible) {
        return "{\"type\":\"" + type + "\",\"id\":\"" + esc(id) + "\",\"name\":\"" + esc(name) +
                "\",\"accessible\":" + accessible + "}";
    }

    private String esc(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
