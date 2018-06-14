package de.maximilianbrandau.intercom.client;

import de.maximilianbrandau.intercom.AlreadyClosedException;
import de.maximilianbrandau.intercom.codec.IntercomCodec;
import de.maximilianbrandau.intercom.codec.NettyCodec;
import de.maximilianbrandau.intercom.codec.packets.PingPacket;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import javax.net.ssl.SSLException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class IntercomClient<T> {

    private static final long RECONNECT_DELAY = 5000;

    final EventLoopGroup eventLoopGroup;
    final HashMap<String, SentRequest<T>> sentRequests;
    final IntercomCodec<T> intercomCodec;
    private final long requestTimeout;
    private final Bootstrap bootstrap;
    private final String host;
    private final int port;
    int ping = -1;
    private ChannelFuture channelFuture;
    private Channel channel;
    private boolean closed = false;
    private long requestId = Long.MIN_VALUE;

    private IntercomClient(String host, int port, boolean ssl, long requestTimeout, IntercomCodec<T> intercomCodec) throws SSLException {
        this.host = host;
        this.port = port;
        this.requestTimeout = requestTimeout;
        this.intercomCodec = intercomCodec;
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
        this.eventLoopGroup = new EpollEventLoopGroup();
        this.bootstrap = new Bootstrap();
        this.bootstrap.group(eventLoopGroup)
                .channel(EpollSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        if (sslCtx != null) {
                            p.addLast(sslCtx.newHandler(ch.alloc(), host, port));
                        }

                        p.addLast("codec", new NettyCodec());
                        p.addLast("handler", new IntercomClientHandler<>(IntercomClient.this));
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
        if (isClosed()) throw new AlreadyClosedException("Client");
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
        if (isClosed()) throw new AlreadyClosedException("Client");
        // TODO: Add authentication implemenation
    }

    String requestId() {
        return String.valueOf(this.requestId++);
    }

    public IntercomRequest.Builder<T> request(String event) {
        if (isClosed()) throw new AlreadyClosedException("Client");
        return new IntercomRequest.Builder<>(this, event, this.channel);
    }

    public int getPing() {
        return ping;
    }

    void ping() {
        if (isClosed()) throw new AlreadyClosedException("Client");
        if (this.channel != null)
            this.channel.writeAndFlush(new PingPacket(System.currentTimeMillis(), ping));
    }

    public long getRequestTimeout() {
        return requestTimeout;
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

        public <T> IntercomClient<T> build(IntercomCodec<T> intercomCodec) throws SSLException {
            return new IntercomClient<>(host, port, ssl, requestTimeout, intercomCodec);
        }

    }

}
