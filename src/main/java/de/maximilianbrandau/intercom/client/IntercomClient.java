package de.maximilianbrandau.intercom.client;

import de.maximilianbrandau.intercom.*;
import de.maximilianbrandau.intercom.authentication.AuthenticationResult;
import de.maximilianbrandau.intercom.authentication.Authenticator;
import de.maximilianbrandau.intercom.codec.IntercomByteBuf;
import de.maximilianbrandau.intercom.codec.IntercomCodec;
import de.maximilianbrandau.intercom.codec.NettyCodec;
import de.maximilianbrandau.intercom.codec.packets.AuthPacket;
import de.maximilianbrandau.intercom.codec.packets.AuthResponsePacket;
import de.maximilianbrandau.intercom.codec.packets.PingPacket;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import javax.annotation.Nullable;
import javax.net.ssl.SSLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class IntercomClient<T, A> {

    private static final long RECONNECT_DELAY = 5000;

    final EventLoopGroup eventLoopGroup;
    final IntercomCodec<T> codec;
    private final RequestFactory<T> requestFactory;
    private final RequestHandlerRegistry<T> requestHandlerRegistry;
    /**
     * Default request timeout in milliseconds
     */
    private final long requestTimeout;
    private final Bootstrap bootstrap;
    private final String host;
    private final int port;
    private final SslContext sslContext;
    private final IntercomCodec<A> authenticationCodec;
    private final Authenticator<A> authenticator;
    private final EventHandler<T> eventHandler;
    int ping = -1;
    AuthenticationResult authenticationResult;
    private ChannelFuture channelFuture;
    private Channel channel;
    private boolean closed = false;
    private CompletableFuture<AuthenticationResult> authenticationResultCompletableFuture;
    private boolean closing = false;

    /**
     * @param host                Host of the server to connect to
     * @param port                Port of the server to connect to
     * @param sslContext          {@link SslContext} to use
     * @param requestTimeout      Default request timeout in milliseconds
     * @param authenticator       {@link Authenticator} to use for authentication at the server
     * @param eventHandler        Handles incoming {@link Event}s from the server
     * @param codec               Encodes/decodes request and event data
     * @param authenticationCodec Encodes/decodes authentication data
     */
    private IntercomClient(String host, int port, SslContext sslContext, long requestTimeout, Authenticator<A> authenticator, IntercomRequestHandler<T> defaultRequestHandler, EventHandler<T> eventHandler, IntercomCodec<T> codec, IntercomCodec<A> authenticationCodec) {
        this.host = host;
        this.port = port;
        this.requestTimeout = requestTimeout;
        this.authenticator = authenticator;
        this.eventHandler = eventHandler;
        this.codec = codec;
        this.authenticationCodec = authenticationCodec;

        this.sslContext = sslContext;

        this.eventLoopGroup = new EpollEventLoopGroup();

        this.requestFactory = new RequestFactory<>(codec, eventLoopGroup, requestTimeout);
        this.requestHandlerRegistry = new RequestHandlerRegistry<>(defaultRequestHandler);

        // Configure the client.
        this.bootstrap = new Bootstrap();
        this.bootstrap.group(eventLoopGroup)
                .channel(EpollSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        if (IntercomClient.this.sslContext != null) {
                            p.addLast(IntercomClient.this.sslContext.newHandler(ch.alloc(), host, port));
                        }

                        p.addLast("codec", new NettyCodec());
                        p.addLast("handler", new IntercomClientHandler<>(IntercomClient.this, IntercomClient.this.eventHandler));
                    }
                });
        connect();
    }

    protected void connect() {
        if (isClosed()) throw new RuntimeException("Client is already closed");
        this.authenticationResult = null;
        try {
            // Start the client.
            this.channelFuture = bootstrap.connect(this.host, this.port).sync().await();
            this.channel = this.channelFuture.channel();

            this.authenticate();
        } catch (Exception e) {
            if (!isClosed()) {
                e.printStackTrace();
                this.eventLoopGroup.schedule(this::connect, RECONNECT_DELAY, TimeUnit.MILLISECONDS);
            }
        }
    }

    private void authenticate() throws ExecutionException, InterruptedException, TimeoutException {
        if (this.authenticator != null) {
            A authenticationData = this.authenticator.authenticate();
            IntercomByteBuf buffer = new IntercomByteBuf(Unpooled.buffer());
            IntercomClient.this.authenticationCodec.encode(authenticationData, buffer);
            AuthPacket authPacket = new AuthPacket(buffer);

            authenticationResultCompletableFuture = new CompletableFuture<>();
            this.channel.writeAndFlush(authPacket);
            this.authenticationResult = authenticationResultCompletableFuture.get(getRequestTimeout(), TimeUnit.MILLISECONDS);
            this.authenticator.handleAuthenticationResult(this.authenticationResult);
        }
    }

    public void close() {
        if (isClosed()) throw new AlreadyClosedException("Client");
        this.closing = true;
        this.eventLoopGroup.shutdownGracefully().awaitUninterruptibly();
        this.closed = true;
    }

    public boolean isClosed() {
        return closed;
    }

    public RequestFactory<T>.Builder request(String route) {
        if (isClosed()) throw new AlreadyClosedException("Client");
        return getRequestFactory().newBuilder(route, this.channel);
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

    protected void handleAuthenticationResponse(AuthResponsePacket authResponsePacket) {
        if (authenticationResultCompletableFuture != null) {
            authenticationResultCompletableFuture.complete(authResponsePacket.getResult());
        }
    }

    RequestFactory<T> getRequestFactory() {
        return requestFactory;
    }

    public RequestHandlerRegistry<T> getRequestHandlerRegistry() {
        return requestHandlerRegistry;
    }

    public boolean isClosing() {
        return closing;
    }

    public static class Builder<T, A> {

        private final String host;
        private final int port;
        private final IntercomCodec<T> codec;
        private final IntercomCodec<A> authenticationCodec;

        private SslContext sslContext;
        /**
         * Clients default request timeout in milliseconds
         */
        private long requestTimeout = 30000;
        private EventHandler<T> eventHandler;
        private Authenticator<A> authenticator;
        private IntercomRequestHandler<T> defaultRequestHandler;

        public Builder(String host, int port, IntercomCodec<T> codec, @Nullable IntercomCodec<A> authenticationCodec) {
            this.host = host;
            this.port = port;
            this.codec = codec;
            this.authenticationCodec = authenticationCodec;
        }

        public Builder<T, A> ssl(SslContext sslContext) throws SSLException {
            if (!sslContext.isClient()) throw new SSLException("SslContext is not a client context");
            this.sslContext = sslContext;
            return this;
        }

        public Builder<T, A> sslInsecureTrustManager() throws SSLException {
            this.sslContext = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
            return this;
        }

        public Builder<T, A> requestTimeout(long requestTimeout) {
            this.requestTimeout = requestTimeout;
            return this;
        }

        public Builder<T, A> authenticator(Authenticator<A> authenticator) {
            this.authenticator = authenticator;
            return this;
        }

        public Builder<T, A> defaultRequestHandler(IntercomRequestHandler<T> handler) {
            this.defaultRequestHandler = handler;
            return this;
        }

        public Builder<T, A> eventHandler(EventHandler<T> eventHandler) {
            this.eventHandler = eventHandler;
            return this;
        }

        public IntercomClient<T, A> build() {
            return new IntercomClient<>(host, port, sslContext, requestTimeout, authenticator, defaultRequestHandler, eventHandler, codec, authenticationCodec);
        }

        /**
         * Copies the {@link Builder}s data to a new one
         *
         * @return The new {@link Builder}
         */
        public Builder<T, A> copy() throws SSLException {
            return new Builder<>(host, port, codec, authenticationCodec)
                    .ssl(this.sslContext)
                    .requestTimeout(this.requestTimeout)
                    .authenticator(this.authenticator)
                    .eventHandler(this.eventHandler);
        }

    }

}
