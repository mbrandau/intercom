package de.maximilianbrandau.intercom;

import io.netty.util.concurrent.ScheduledFuture;

import java.util.concurrent.CompletableFuture;

public class SentRequest<T> {

    private final long startTime;
    private final int requestId;
    private final T data;
    private final ScheduledFuture timeoutFuture;
    private final CompletableFuture<Response<T>> future;

    SentRequest(long startTime, int requestId, T data, ScheduledFuture timeoutFuture, CompletableFuture<Response<T>> future) {
        this.startTime = startTime;
        this.requestId = requestId;
        this.data = data;
        this.timeoutFuture = timeoutFuture;
        this.future = future;
    }

    public long getStartTime() {
        return startTime;
    }

    public int getRequestId() {
        return requestId;
    }

    public T getData() {
        return data;
    }

    ScheduledFuture getTimeoutFuture() {
        return timeoutFuture;
    }

    public CompletableFuture<Response<T>> getFuture() {
        return future;
    }

}
