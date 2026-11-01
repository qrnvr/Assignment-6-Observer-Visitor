package com.arnur.transport.visitor.operation;

import com.arnur.transport.visitor.*;
import com.arnur.transport.visitor.element.*;
import java.math.BigDecimal;

public final class TicketRevenueVisitor implements TransportVisitor<BigDecimal> {
    @Override public BigDecimal visitNetwork(TransportNetwork network) {
        return network.routes().stream().map(route -> route.accept(this)).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    @Override public BigDecimal visitRoute(Route route) {
        return route.stops().stream().map(stop -> stop.accept(this)).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    @Override public BigDecimal visitStop(Stop stop) {
        return stop.elements().stream().map(element -> element.accept(this)).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    @Override public BigDecimal visitBus(Bus bus) { return bus.ticketRevenue(); }
    @Override public BigDecimal visitTram(Tram tram) { return tram.ticketRevenue(); }
    @Override public BigDecimal visitStationFacility(StationFacility station) { return station.ticketRevenue(); }
}
