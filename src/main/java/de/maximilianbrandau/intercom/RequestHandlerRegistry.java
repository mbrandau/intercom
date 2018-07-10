package de.maximilianbrandau.intercom;

import java.util.HashMap;

public class RequestHandlerRegistry<T> {

    private final HashMap<String, IntercomRequestHandler<T>> handlers;
    private final IntercomRequestHandler<T> defaultHandler;

    public RequestHandlerRegistry(IntercomRequestHandler<T> defaultHandler) {
        this.defaultHandler = defaultHandler;
        this.handlers = new HashMap<>();
    }

    public void registerHandler(String route, IntercomRequestHandler<T> handler) {
        handlers.put(route, handler);
    }

    public boolean hasHandler(String route) {
        return handlers.containsKey(route);
    }

    public void unregisterHandler(String route) {
        handlers.remove(route);
    }

    public IntercomRequestHandler<T> getHandler(String route) {
        return handlers.get(route);
    }

    public IntercomRequestHandler<T> getDefaultHandler() {
        return defaultHandler;
    }

    public IntercomRequestHandler<T> getHandlerOrDefault(String route) {
        return handlers.getOrDefault(route, this.defaultHandler);
    }

}
