package com.arnur.transport.visitor.element;

import com.arnur.transport.visitor.*;
import java.util.List;

public record Stop(String id, String name, List<TransportElement> elements) implements TransportElement {
    public Stop {
        elements = List.copyOf(elements);
    }
    @Override public <R> R accept(TransportVisitor<R> visitor) { return visitor.visitStop(this); }
}
