package de.maximilianbrandau.intercom;

public interface Handler<T> {
    void handle(T data);
}
