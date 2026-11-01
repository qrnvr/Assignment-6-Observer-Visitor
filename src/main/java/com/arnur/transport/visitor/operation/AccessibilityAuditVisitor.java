package com.arnur.transport.visitor.operation;

import com.arnur.transport.visitor.*;
import com.arnur.transport.visitor.element.*;
import com.arnur.transport.visitor.result.AccessibilityAudit;
import java.util.List;

public final class AccessibilityAuditVisitor implements TransportVisitor<AccessibilityAudit> {
    @Override public AccessibilityAudit visitNetwork(TransportNetwork network) {
        return network.routes().stream().map(route -> route.accept(this)).reduce(AccessibilityAudit.empty(), AccessibilityAudit::plus);
    }
    @Override public AccessibilityAudit visitRoute(Route route) {
        return route.stops().stream().map(stop -> stop.accept(this)).reduce(AccessibilityAudit.empty(), AccessibilityAudit::plus);
    }
    @Override public AccessibilityAudit visitStop(Stop stop) {
        return stop.elements().stream().map(element -> element.accept(this)).reduce(AccessibilityAudit.empty(), AccessibilityAudit::plus);
    }
    @Override public AccessibilityAudit visitBus(Bus bus) { return leaf(bus.id(), bus.accessible()); }
    @Override public AccessibilityAudit visitTram(Tram tram) { return leaf(tram.id(), tram.accessible()); }
    @Override public AccessibilityAudit visitStationFacility(StationFacility station) { return leaf(station.id(), station.accessible()); }
    private AccessibilityAudit leaf(String id, boolean accessible) {
        return new AccessibilityAudit(1, accessible ? 1 : 0, accessible ? List.of() : List.of(id + " is not accessible"));
    }
}
