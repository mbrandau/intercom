package de.maximilianbrandau.intercom.client;

import io.netty.util.concurrent.ScheduledFuture;

public class SentRequest<T> {

    private final long startTime;
    private final String requestId;
    private final T data;
    private final ScheduledFuture future;
    private final IntercomResponseHandler<T> responseHandler;

    SentRequest(long startTime, String requestId, T data, ScheduledFuture future, IntercomResponseHandler<T> responseHandler) {
        this.startTime = startTime;
        this.requestId = requestId;
        this.data = data;
        this.future = future;
        this.responseHandler = responseHandler;
    }

    public long getStartTime() {
        return startTime;
    }

    public String getRequestId() {
        return requestId;
    }

    public T getData() {
        return data;
    }

    public ScheduledFuture getFuture() {
        return future;
    }

    public IntercomResponseHandler<T> getResponseHandler() {
        return responseHandler;
    }
}
