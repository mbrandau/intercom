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
