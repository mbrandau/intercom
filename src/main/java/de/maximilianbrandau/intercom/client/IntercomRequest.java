package de.maximilianbrandau.intercom.client;

import de.maximilianbrandau.intercom.Handler;
import de.maximilianbrandau.intercom.codec.IntercomByteBuf;
import de.maximilianbrandau.intercom.codec.packets.RequestPacket;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.util.concurrent.ScheduledFuture;

import java.util.concurrent.TimeUnit;

public class IntercomRequest<T> {

    private final IntercomClient<T> client;
    private final long startTime;
    private final long requestTimeout;
    private final String requestId, event;
    private final T data;
    private final Handler<IntercomResponse<T>> responseHandler;
    private final Handler<Throwable> errorHandler;
    private final Channel channel;

    private final ScheduledFuture future;

    private IntercomRequest(IntercomClient<T> client, long requestTimeout, String event, T data, Handler<IntercomResponse<T>> responseHandler, Handler<Throwable> errorHandler, Channel channel) {
        this.client = client;
        this.requestTimeout = requestTimeout;
        this.errorHandler = errorHandler;
        this.channel = channel;
        this.requestId = this.client.requestId();
        this.event = event;
        this.data = data;
        this.responseHandler = responseHandler;
        this.future = client.eventLoopGroup.schedule(() -> {
            SentRequest<T> sentRequest = client.sentRequests.get(requestId);
            if (sentRequest != null) {
                sentRequest.getResponseHandler().handleError(new RequestTimeoutException());
                client.sentRequests.remove(requestId);
            }
        }, requestTimeout, TimeUnit.MILLISECONDS);

        IntercomByteBuf dataBuffer = new IntercomByteBuf(Unpooled.buffer());
        this.client.encodingMechanism.encode(data, dataBuffer);

        RequestPacket packet = new RequestPacket(requestId, event, dataBuffer);
        this.startTime = System.currentTimeMillis();

        this.client.sentRequests.put(requestId, new SentRequest<>(startTime, requestId, data, future, new IntercomResponseHandler<T>() {
            @Override
            public void handleResponse(IntercomResponse<T> response) {
                if (responseHandler != null) responseHandler.handle(response);
            }

            @Override
            public void handleError(Throwable throwable) {
                if (errorHandler != null) errorHandler.handle(throwable);
            }
        }));
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

    public String getRequestId() {
        return requestId;
    }

    public String getEvent() {
        return event;
    }

    public Handler<IntercomResponse<T>> getResponseHandler() {
        return responseHandler;
    }

    public Handler<Throwable> getErrorHandler() {
        return errorHandler;
    }

    public ScheduledFuture getFuture() {
        return future;
    }

    public static class Builder<T> {

        private final IntercomClient<T> client;
        private final Channel channel;
        private long requestTimeout = -1;
        private String event;
        private T data;
        private Handler<IntercomResponse<T>> responseHandler = null;
        private Handler<Throwable> errorHandler = null;


        Builder(IntercomClient<T> client, String event, Channel channel) {
            this.client = client;
            this.event = event;
            this.channel = channel;
        }

        public Builder<T> data(T data) {
            this.data = data;
            return this;
        }

        public Builder<T> requestTimeout(long timeout) {
            this.requestTimeout = timeout;
            return this;
        }

        public Builder<T> response(Handler<IntercomResponse<T>> responseHandler) {
            this.responseHandler = responseHandler;
            return this;
        }

        public Builder<T> error(Handler<Throwable> errorHandler) {
            this.errorHandler = errorHandler;
            return this;
        }

        public IntercomRequest<T> build() {
            return new IntercomRequest<>(
                    this.client,
                    this.requestTimeout >= 0 ? this.requestTimeout : this.client.getRequestTimeout(),
                    this.event,
                    this.data,
                    this.responseHandler,
                    this.errorHandler,
                    this.channel
            );
        }

    }

}
