package de.maximilianbrandau.intercom.client;

import io.netty.util.concurrent.ScheduledFuture;

import java.util.concurrent.CompletableFuture;

public class SentRequest<T> {

    private final long startTime;
    private final String requestId;
    private final T data;
    private final ScheduledFuture timeoutFuture;
    private final CompletableFuture<IntercomResponse<T>> future;

    SentRequest(long startTime, String requestId, T data, ScheduledFuture timeoutFuture, CompletableFuture<IntercomResponse<T>> future) {
        this.startTime = startTime;
        this.requestId = requestId;
        this.data = data;
        this.timeoutFuture = timeoutFuture;
        this.future = future;
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

    ScheduledFuture getTimeoutFuture() {
        return timeoutFuture;
    }

    public CompletableFuture<IntercomResponse<T>> getFuture() {
        return future;
    }

}
