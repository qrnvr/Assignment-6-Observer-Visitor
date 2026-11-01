package com.arnur.transport.visitor;

import com.arnur.transport.visitor.element.*;

public interface TransportVisitor<R> {
    R visitNetwork(TransportNetwork network);
    R visitRoute(Route route);
    R visitStop(Stop stop);
    R visitBus(Bus bus);
    R visitTram(Tram tram);
    R visitStationFacility(StationFacility station);
}
