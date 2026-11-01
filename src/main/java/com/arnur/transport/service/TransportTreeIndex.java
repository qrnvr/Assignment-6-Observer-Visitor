package com.arnur.transport.service;

import com.arnur.transport.visitor.element.Route;
import com.arnur.transport.visitor.element.TransportNetwork;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class TransportTreeIndex {
    private final Map<String, Route> routes;

    public TransportTreeIndex(TransportNetwork network) {
        this.routes = network.routes().stream().collect(Collectors.toUnmodifiableMap(Route::id, Function.identity()));
    }

    public Optional<Route> route(String id) {
        return Optional.ofNullable(routes.get(id));
    }
}
