package com.arnur.transport.visitor;

public interface TransportElement {
    String id();
    String name();
    <R> R accept(TransportVisitor<R> visitor);
}
