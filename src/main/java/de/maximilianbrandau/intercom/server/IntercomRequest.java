package de.maximilianbrandau.intercom.server;

import java.net.InetSocketAddress;

public class IntercomRequest<T> {

    private final long receiveTime;
    private final InetSocketAddress address;
    private final boolean authenticated;
    private final String requestId;
    private final String event;
    private final T data;

    IntercomRequest(long receiveTime, InetSocketAddress address, boolean authenticated, String requestId, String event, T data) {
        this.receiveTime = receiveTime;
        this.address = address;
        this.authenticated = authenticated;
        this.requestId = requestId;
        this.event = event;
        this.data = data;
    }

    public long getReceiveTime() {
        return receiveTime;
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getEvent() {
        return event;
    }

    public T getData() {
        return data;
    }

}
