package de.maximilianbrandau.intercom.server;

import com.google.common.collect.ImmutableList;
import de.maximilianbrandau.intercom.AlreadyClosedException;
import de.maximilianbrandau.intercom.Event;
import de.maximilianbrandau.intercom.IntercomRequestHandler;
import de.maximilianbrandau.intercom.RequestHandlerRegistry;
import de.maximilianbrandau.intercom.authentication.AuthenticationHandler;
import de.maximilianbrandau.intercom.codec.IntercomByteBuf;
import de.maximilianbrandau.intercom.codec.IntercomCodec;
import de.maximilianbrandau.intercom.codec.packets.PushPacket;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.util.concurrent.GlobalEventExecutor;

import javax.annotation.Nullable;
import javax.net.ssl.SSLException;
import java.io.File;
import java.security.cert.CertificateException;
import java.util.LinkedList;
import java.util.List;

public class IntercomServer<T, A, C extends Client> {

    final IntercomCodec<T> codec;
    final IntercomCodec<A> authenticationCodec;
    final long requestTimeout;
    final AuthenticationHandler<A> authenticationHandler;
    final EventLoopGroup workerGroup;
    private final RequestHandlerRegistry<T> requestHandlerRegistry;
    private final String host;
    private final int port;

    private final ChannelFuture channelFuture;
    private final SslContext sslContext;
    private final ChannelGroup channels;
    private EventLoopGroup bossGroup;
    private boolean closed = false;

    private LinkedList<C> clients;

    private IntercomServer(String host, int port, SslContext sslContext, IntercomRequestHandler<T> defaultRequestHandler, AuthenticationHandler<A> authenticationHandler, IntercomCodec<T> codec, IntercomCodec<A> authenticationCodec, long requestTimeout, ClientInitializer<T, A, C> clientInitializer) {
        this.host = host;
        this.port = port;
        this.sslContext = sslContext;
        this.authenticationHandler = authenticationHandler;
        this.codec = codec;
        this.authenticationCodec = authenticationCodec;
        this.requestTimeout = requestTimeout;

        this.bossGroup = new EpollEventLoopGroup(1);
        this.workerGroup = new EpollEventLoopGroup();

        this.requestHandlerRegistry = new RequestHandlerRegistry<>(defaultRequestHandler);

        this.clients = new LinkedList<>();
        this.channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(EpollServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 100)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();

                        // Add an SslHandler if an SslContext is given
                        if (IntercomServer.this.sslContext != null)
                            p.addLast(IntercomServer.this.sslContext.newHandler(ch.alloc()));

                        IntercomServer.this.channels.add(ch);
                        clients.add(clientInitializer.init(IntercomServer.this, ch));
                    }
                });


        // Bind and start to accept incoming connections.
        ChannelFuture bindFuture;
        if (this.host == null) bindFuture = b.bind(port);
        else bindFuture = b.bind(this.host, port);
        try {
            bindFuture = bindFuture.sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Channel channel = bindFuture.channel();
        this.channelFuture = bindFuture.channel().closeFuture();
    }

    public List<C> getClients() {
        return ImmutableList.<C>builder().addAll(clients).build();
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public void close() {
        if (isClosed()) throw new AlreadyClosedException("Server");
        if (this.channelFuture != null)
            this.channelFuture.channel().close().channel().closeFuture();
        this.bossGroup.shutdownGracefully();
        this.workerGroup.shutdownGracefully();
        this.closed = true;
    }

    public boolean isClosed() {
        return closed;
    }

    /**
     * Pushes an {@link Event} to all connected clients
     *
     * @param event The {@link Event} to push to the clients
     */
    public void pushEvent(Event<T> event) {
        if (isClosed()) throw new AlreadyClosedException("Server");

        IntercomByteBuf dataBuffer = new IntercomByteBuf(Unpooled.buffer());
        this.codec.encode(event.getData(), dataBuffer);

        this.channels.writeAndFlush(new PushPacket(event.getEvent(), dataBuffer)).awaitUninterruptibly();
    }

    public RequestHandlerRegistry<T> getRequestHandlerRegistry() {
        return requestHandlerRegistry;
    }

    public static class Builder<T, A> {

        private final int port;
        private final IntercomCodec<T> codec;
        private final IntercomCodec<A> authenticationCodec;

        private String host = null;
        private SslContext sslContext;
        private long requestTimeout = 30000;
        private AuthenticationHandler<A> authenticationHandler = null;
        private IntercomRequestHandler<T> routeNotFoundHandler;

        /**
         * Creates a {@link Builder} for an {@link IntercomServer}.
         *
         * @param port                The port the server should listen to.
         * @param codec               The {@link IntercomCodec} to encode and decode request and pushEvent event data
         * @param authenticationCodec The {@link IntercomCodec} to encode and decode authentication data. Only required if an {@link AuthenticationHandler} is then set via {@code Builder.authenticationHandler(handler)}.
         */
        public Builder(int port, IntercomCodec<T> codec, @Nullable IntercomCodec<A> authenticationCodec) {
            this.port = port;
            this.codec = codec;
            this.authenticationCodec = authenticationCodec;
        }

        /**
         * Sets the hostname for the server to listen to.
         *
         * @param host The hostname the server should listen to
         * @return Returns this {@link Builder}
         */
        public Builder<T, A> host(String host) {
            this.host = host;
            return this;
        }

        /**
         * Supply an {@link SslContext} to use.
         *
         * @param sslContext The {@link SslContext} to use
         * @return Returns this {@link Builder}
         * @throws SSLException if the {@link SslContext} is not a server context
         */
        public Builder<T, A> ssl(SslContext sslContext) throws SSLException {
            if (!sslContext.isServer()) throw new SSLException("SslContext is not a server context");
            this.sslContext = sslContext;
            return this;
        }

        /**
         * @param keyCertChainFile
         * @param keyFile
         * @return Returns this {@link Builder}
         * @throws SSLException
         */
        public Builder<T, A> ssl(File keyCertChainFile, File keyFile) throws SSLException {
            this.sslContext = SslContextBuilder.forServer(keyCertChainFile, keyFile).build();
            return this;
        }

        /**
         * @param keyCertChainFile
         * @param keyFile
         * @param keyPassword
         * @return Returns this {@link Builder}
         * @throws SSLException
         */
        public Builder<T, A> ssl(File keyCertChainFile, File keyFile, String keyPassword) throws SSLException {
            this.sslContext = SslContextBuilder.forServer(keyCertChainFile, keyFile, keyPassword).build();
            return this;
        }

        /**
         * Generates a self signed certificate to use for encryption
         *
         * @return Returns this {@link Builder}
         * @throws CertificateException
         * @throws SSLException
         */
        public Builder<T, A> sslSelfSignedCertificate() throws CertificateException, SSLException {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            this.sslContext = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
            return this;
        }

        /**
         * Set an {@link IntercomRequestHandler} to handle routes that were not found.
         *
         * @param handler The {@link IntercomRequestHandler} to handle routes that were not found
         * @return Returns this {@link Builder}
         */
        public Builder<T, A> defaultRequestHandler(IntercomRequestHandler<T> handler) {
            this.routeNotFoundHandler = handler;
            return this;
        }

        public Builder<T, A> requestTimeout(long requestTimeout) {
            this.requestTimeout = requestTimeout;
            return this;
        }

        /**
         * Sets the {@link AuthenticationHandler} to handle incoming authentication requests.
         * Requires an {@link IntercomCodec} for the authentication data to be set.
         *
         * @param authenticationHandler Handles the incoming authentication requests
         * @return Returns this {@link Builder}
         */
        public Builder<T, A> authenticationHandler(AuthenticationHandler<A> authenticationHandler) {
            this.authenticationHandler = authenticationHandler;
            return this;
        }

        /**
         * Builds and starts the {@link IntercomServer}
         *
         * @return The started {@link IntercomServer}
         */
        public <C extends Client> IntercomServer<T, A, C> build(ClientInitializer<T, A, C> clientInitializer) {
            return new IntercomServer<>(host, port, sslContext, routeNotFoundHandler, authenticationHandler, codec, authenticationCodec, requestTimeout, clientInitializer);
        }

        /**
         * Copies the {@link Builder}s data to a new one
         *
         * @return The new {@link Builder}
         * @throws SSLException
         */
        public Builder<T, A> copy() throws SSLException {
            return new Builder<>(port, codec, authenticationCodec)
                    .host(this.host)
                    .ssl(this.sslContext)
                    .defaultRequestHandler(this.routeNotFoundHandler)
                    .authenticationHandler(this.authenticationHandler);
        }
    }
}
