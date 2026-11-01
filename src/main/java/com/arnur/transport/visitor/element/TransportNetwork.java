package com.arnur.transport.visitor.element;

import com.arnur.transport.visitor.*;
import java.util.List;

public record TransportNetwork(String id, String name, List<Route> routes) implements TransportElement {
    public TransportNetwork {
        routes = List.copyOf(routes);
    }
    @Override public <R> R accept(TransportVisitor<R> visitor) { return visitor.visitNetwork(this); }
}
