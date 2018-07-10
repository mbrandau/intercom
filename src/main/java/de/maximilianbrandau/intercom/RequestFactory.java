package de.maximilianbrandau.intercom;

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

        public Builder data(T data) {
            this.data = data;
            return this;
        }

        public Builder requestTimeout(long timeout) {
            this.requestTimeout = timeout;
            return this;
        }

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
