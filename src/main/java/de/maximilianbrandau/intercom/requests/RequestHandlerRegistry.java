/*
 * Copyright (c) 2017-2018 Maximilian Brandau
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package de.maximilianbrandau.intercom.requests;

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
