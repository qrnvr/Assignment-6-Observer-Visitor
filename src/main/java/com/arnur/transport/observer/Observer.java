package com.arnur.transport.observer;

@FunctionalInterface
public interface Observer<E> {
    void onEvent(E event);
}
