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

import de.maximilianbrandau.intercom.codec.IntercomByteBuf;
import de.maximilianbrandau.intercom.codec.IntercomCodec;
import de.maximilianbrandau.intercom.codec.packets.RequestPacket;
import de.maximilianbrandau.intercom.codec.packets.ResponsePacket;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.ScheduledFuture;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class RequestFactory<T> {

    private final IntercomCodec<T> codec;
    private final HashMap<Integer, SentRequest<T>> sentRequests;
    private final EventLoopGroup eventLoopGroup;
    private final long requestTimeout;
    private int requestId = Integer.MIN_VALUE;

    public RequestFactory(IntercomCodec<T> codec, EventLoopGroup eventLoopGroup, long requestTimeout) {
        this.codec = codec;
        this.eventLoopGroup = eventLoopGroup;
        this.requestTimeout = requestTimeout;
        this.sentRequests = new HashMap<>();
    }

    public IntercomCodec<T> getCodec() {
        return codec;
    }

    public int requestId() {
        if (this.requestId == Integer.MAX_VALUE) this.requestId = Integer.MIN_VALUE;
        return this.requestId++;
    }

    public long getRequestTimeout() {
        return requestTimeout;
    }

    public Builder newBuilder(String route, Channel channel) {
        return new Builder(this, route, channel);
    }

    public void handleResponse(ResponsePacket packet, long receiveTime) {
        SentRequest<T> sentRequest = this.sentRequests.get(packet.getRequestId());
        if (sentRequest != null) {
            try {
                Response<T> response = new Response<>(
                        packet.getStatus(),
                        receiveTime - sentRequest.getStartTime(),
                        this.getCodec().decode(packet.getData())
                );
                sentRequest.getFuture().complete(response);
            } catch (Throwable t) {
                sentRequest.getFuture().completeExceptionally(t);
            } finally {
                sentRequest.getTimeoutFuture().cancel(false);
                this.sentRequests.remove(packet.getRequestId());
            }
        }
    }

    public class Builder {

        private final RequestFactory<T> requestFactory;
        private final Channel channel;
        private final String route;
        private final CompletableFuture<Response<T>> future;
        private long requestTimeout = RequestFactory.this.requestTimeout;
        private T data;

        private Builder(RequestFactory<T> requestFactory, String route, Channel channel) {
            this.requestFactory = requestFactory;
            this.route = route;
            this.channel = channel;
            this.future = new CompletableFuture<>();
        }

        /**
         * Sets the request data that will be send to the specified route
         *
         * @param data Request data
         * @return Returns this {@link Builder}
         */
        public Builder data(T data) {
            this.data = data;
            return this;
        }

        /**
         * Sets the request timeout to use. If not specified the default request timeout of the {@link de.maximilianbrandau.intercom.client.IntercomClient}/{@link de.maximilianbrandau.intercom.server.IntercomServer} will be used.
         *
         * @param timeout  Request timeout
         * @param timeUnit {@link TimeUnit} of the given request timeout
         * @return Returns this {@link Builder}
         */
        public Builder requestTimeout(long timeout, TimeUnit timeUnit) {
            this.requestTimeout = timeUnit.toMillis(timeout);
            return this;
        }

        /**
         * Sends the request
         *
         * @return {@link CompletableFuture} that completes when a {@link Response} was received or that completes exceptionally when the request timed out.
         */
        public CompletableFuture<Response<T>> send() {
            int requestId = this.requestFactory.requestId();
            ScheduledFuture timeoutFuture = requestFactory.eventLoopGroup.schedule(() -> {
                SentRequest<T> sentRequest = requestFactory.sentRequests.get(requestId);
                if (sentRequest != null) {
                    sentRequest.getFuture().completeExceptionally(new RequestTimeoutException(System.currentTimeMillis() - sentRequest.getStartTime()));
                    requestFactory.sentRequests.remove(requestId);
                }
            }, requestTimeout, TimeUnit.MILLISECONDS);


            IntercomByteBuf dataBuffer = new IntercomByteBuf(Unpooled.buffer());
            this.requestFactory.getCodec().encode(data, dataBuffer);

            RequestPacket packet = new RequestPacket(requestId, route, dataBuffer);
            long startTime = System.currentTimeMillis();

            this.requestFactory.sentRequests.put(requestId, new SentRequest<>(startTime, requestId, data, timeoutFuture, this.future));
            this.channel.writeAndFlush(packet).awaitUninterruptibly();
            return future;
        }
    }
}
