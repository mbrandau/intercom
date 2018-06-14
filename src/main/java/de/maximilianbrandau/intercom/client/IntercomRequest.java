package de.maximilianbrandau.intercom.client;

import de.maximilianbrandau.intercom.codec.IntercomByteBuf;
import de.maximilianbrandau.intercom.codec.packets.RequestPacket;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.util.concurrent.ScheduledFuture;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class IntercomRequest<T> {

    private final IntercomClient<T> client;
    private final long startTime;
    private final long requestTimeout;
    private final String requestId, event;
    private final T data;
    private final Channel channel;

    private final ScheduledFuture timeoutFuture;
    private final CompletableFuture<IntercomResponse<T>> future;

    private IntercomRequest(IntercomClient<T> client, long requestTimeout, String event, T data, Channel channel, CompletableFuture<IntercomResponse<T>> future) {
        this.client = client;
        this.requestTimeout = requestTimeout;
        this.channel = channel;
        this.future = future;
        this.requestId = this.client.requestId();
        this.event = event;
        this.data = data;
        this.timeoutFuture = client.eventLoopGroup.schedule(() -> {
            SentRequest<T> sentRequest = client.sentRequests.get(requestId);
            if (sentRequest != null) {
                sentRequest.getFuture().completeExceptionally(new RequestTimeoutException(System.currentTimeMillis() - sentRequest.getStartTime()));
                client.sentRequests.remove(requestId);
            }
        }, requestTimeout, TimeUnit.MILLISECONDS);

        IntercomByteBuf dataBuffer = new IntercomByteBuf(Unpooled.buffer());
        this.client.intercomCodec.encode(data, dataBuffer);

        RequestPacket packet = new RequestPacket(requestId, event, dataBuffer);
        this.startTime = System.currentTimeMillis();

        this.client.sentRequests.put(requestId, new SentRequest<>(startTime, requestId, data, timeoutFuture, this.future));
        try {
            this.channel.writeAndFlush(packet).sync().await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public long getRequestTimeout() {
        return requestTimeout;
    }

    public long getStartTime() {
        return startTime;
    }

    public T getData() {
        return data;
    }

    public String getEvent() {
        return event;
    }

    public static class Builder<T> {

        private final IntercomClient<T> client;
        private final Channel channel;
        private long requestTimeout = -1;
        private String event;
        private T data;
        private CompletableFuture<IntercomResponse<T>> future;

        Builder(IntercomClient<T> client, String event, Channel channel) {
            this.client = client;
            this.event = event;
            this.channel = channel;
            this.future = new CompletableFuture<>();
        }

        public Builder<T> data(T data) {
            this.data = data;
            return this;
        }

        public Builder<T> requestTimeout(long timeout) {
            this.requestTimeout = timeout;
            return this;
        }

        public CompletableFuture<IntercomResponse<T>> send() {
            IntercomRequest<T> request = new IntercomRequest<>(
                    this.client,
                    this.requestTimeout >= 0 ? this.requestTimeout : this.client.getRequestTimeout(),
                    this.event,
                    this.data,
                    this.channel,
                    this.future);
            return future;
        }

    }

}
