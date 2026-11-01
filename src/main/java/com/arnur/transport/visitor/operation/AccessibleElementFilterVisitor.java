package com.arnur.transport.visitor.operation;

import com.arnur.transport.visitor.*;
import com.arnur.transport.visitor.element.*;
import java.util.ArrayList;
import java.util.List;

public final class AccessibleElementFilterVisitor implements TransportVisitor<List<TransportElement>> {
    @Override public List<TransportElement> visitNetwork(TransportNetwork network) {
        List<TransportElement> result = new ArrayList<>();
        network.routes().forEach(route -> result.addAll(route.accept(this)));
        return List.copyOf(result);
    }
    @Override public List<TransportElement> visitRoute(Route route) {
        List<TransportElement> result = new ArrayList<>();
        route.stops().forEach(stop -> result.addAll(stop.accept(this)));
        return List.copyOf(result);
    }
    @Override public List<TransportElement> visitStop(Stop stop) {
        List<TransportElement> result = new ArrayList<>();
        stop.elements().forEach(element -> result.addAll(element.accept(this)));
        return List.copyOf(result);
    }
    @Override public List<TransportElement> visitBus(Bus bus) { return bus.accessible() ? List.of(bus) : List.of(); }
    @Override public List<TransportElement> visitTram(Tram tram) { return tram.accessible() ? List.of(tram) : List.of(); }
    @Override public List<TransportElement> visitStationFacility(StationFacility station) { return station.accessible() ? List.of(station) : List.of(); }
    @Override public List<TransportElement> visitTicketMachine(TicketMachine machine) { return machine.accessible() ? List.of(machine) : List.of(); }
}
