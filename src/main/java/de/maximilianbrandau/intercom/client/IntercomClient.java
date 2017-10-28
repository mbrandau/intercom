package de.maximilianbrandau.intercom.client;

import de.maximilianbrandau.intercom.encoding.EncodingMechanism;
import de.maximilianbrandau.intercom.encoding.net.IntercomDecoder;
import de.maximilianbrandau.intercom.encoding.net.IntercomEncoder;
import de.maximilianbrandau.intercom.encoding.net.packets.PingPacket;
import de.maximilianbrandau.intercom.encoding.net.packets.RequestPacket;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.concurrent.ScheduledFuture;

import javax.net.ssl.SSLException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class IntercomClient<T> {

    private static final long RECONNECT_DELAY = 5000;

    protected final EventLoopGroup eventLoopGroup;
    protected final HashMap<String, SentRequest<T>> sentRequests;
    protected final EncodingMechanism<T> encodingMechanism;
    private final long requestTimeout;
    private final Bootstrap bootstrap;
    private final String host;
    private final int port;
    protected int ping = -1;
    private ChannelFuture channelFuture;
    private Channel channel;
    private boolean closed = false;
    private long requestId = Long.MIN_VALUE;

    public IntercomClient(String host, int port, boolean ssl, long requestTimeout, EncodingMechanism<T> encodingMechanism) throws SSLException {
        this.host = host;
        this.port = port;
        this.requestTimeout = requestTimeout;
        this.encodingMechanism = encodingMechanism;
        // Configure SSL.git
        final SslContext sslCtx;
        if (ssl) {
            sslCtx = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        } else {
            sslCtx = null;
        }

        this.sentRequests = new HashMap<>();

        // Configure the client.
        this.eventLoopGroup = new NioEventLoopGroup();
        this.bootstrap = new Bootstrap();
        this.bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        if (sslCtx != null) {
                            p.addLast(sslCtx.newHandler(ch.alloc(), host, port));
                        }

                        p.addLast(new IntercomDecoder());
                        p.addLast(new IntercomEncoder());
                        p.addLast(new IntercomClientHandler<>(IntercomClient.this));
                    }
                });
        connect();
    }

    protected void connect() {
        if (isClosed()) throw new RuntimeException("Client is already closed");
        try {
            // Start the client.
            this.channelFuture = bootstrap.connect(this.host, this.port).sync().await();
            this.channel = this.channelFuture.channel();
        } catch (Exception e) {
            if (!isClosed()) {
                e.printStackTrace();
                this.eventLoopGroup.schedule(this::connect, RECONNECT_DELAY, TimeUnit.MILLISECONDS);
            }
        }
    }

    public void close() {
        if (channelFuture != null)
            try {
                channelFuture.channel().close().channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        this.eventLoopGroup.shutdownGracefully();
        closed = false;
    }

    public boolean isClosed() {
        return closed;
    }

    public void authenticate() {

    }

    public void request(String event, T data, IntercomResponseHandler<T> responseHandler) {
        long startTime = System.currentTimeMillis();
        String requestId = String.valueOf(this.requestId++);

        ByteBuf dataBuffer = Unpooled.buffer();
        this.encodingMechanism.encode(data, dataBuffer);

        RequestPacket packet = new RequestPacket(requestId, event, dataBuffer);

        this.channel.writeAndFlush(packet);

        ScheduledFuture future = this.eventLoopGroup.schedule(() -> {
            SentRequest<T> sentRequest = sentRequests.get(requestId);
            if (sentRequest != null) {
                sentRequest.getResponseHandler().handleError(new RequestTimeoutException());
                sentRequests.remove(requestId);
            }
        }, requestTimeout, TimeUnit.MILLISECONDS);
        SentRequest<T> sentRequest = new SentRequest<>(startTime, requestId, data, future, responseHandler);
        this.sentRequests.put(requestId, sentRequest);
    }

    public int getPing() {
        return ping;
    }

    protected void ping() {
        if (IntercomClient.this.channel != null)
            IntercomClient.this.channel.writeAndFlush(new PingPacket(System.currentTimeMillis(), ping));
    }

    public static class Builder {

        private String host;
        private int port;
        private boolean ssl = true;
        private long requestTimeout = 30000;

        public Builder(String host, int port) {
            this.host = host;
            this.port = port;
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder ssl(boolean ssl) {
            this.ssl = ssl;
            return this;
        }

        public Builder requestTimeout(long requestTimeout) {
            this.requestTimeout = requestTimeout;
            return this;
        }

        public <T> IntercomClient<T> build(EncodingMechanism<T> encodingMechanism) throws SSLException {
            return new IntercomClient<>(host, port, ssl, requestTimeout, encodingMechanism);
        }

    }

}
