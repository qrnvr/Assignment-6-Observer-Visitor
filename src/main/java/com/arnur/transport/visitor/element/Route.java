package com.arnur.transport.visitor.element;

import com.arnur.transport.visitor.*;
import java.util.List;

public record Route(String id, String name, List<Stop> stops) implements TransportElement {
    public Route {
        stops = List.copyOf(stops);
    }
    @Override public <R> R accept(TransportVisitor<R> visitor) { return visitor.visitRoute(this); }
}
