package de.maximilianbrandau.intercom;

import de.maximilianbrandau.intercom.authentication.AuthenticationResult;

import java.net.InetSocketAddress;

public class Request<T> {

    private final long receiveTime;
    private final InetSocketAddress address;
    private final AuthenticationResult authenticationResult;
    private final int requestId;
    private final String event;
    private final T data;

    public Request(long receiveTime, InetSocketAddress address, AuthenticationResult authenticationResult, int requestId, String event, T data) {
        this.receiveTime = receiveTime;
        this.address = address;
        this.authenticationResult = authenticationResult;
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

    public AuthenticationResult getAuthenticationResult() {
        return authenticationResult;
    }

    public int getRequestId() {
        return requestId;
    }

    public String getEvent() {
        return event;
    }

    public T getData() {
        return data;
    }

}
