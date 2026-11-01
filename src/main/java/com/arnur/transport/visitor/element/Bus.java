package com.arnur.transport.visitor.element;

import com.arnur.transport.visitor.*;
import java.math.BigDecimal;

public record Bus(String id, String name, boolean accessible, BigDecimal ticketRevenue) implements TransportElement {
    @Override public <R> R accept(TransportVisitor<R> visitor) { return visitor.visitBus(this); }
}
