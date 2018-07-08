package de.maximilianbrandau.intercom.server;

import de.maximilianbrandau.intercom.AlreadyClosedException;
import de.maximilianbrandau.intercom.AuthenticationHandler;
import de.maximilianbrandau.intercom.codec.IntercomByteBuf;
import de.maximilianbrandau.intercom.codec.IntercomCodec;
import de.maximilianbrandau.intercom.codec.NettyCodec;
import de.maximilianbrandau.intercom.codec.packets.PushPacket;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;
import java.util.HashMap;

public class IntercomServer<T> {

    final HashMap<String, IntercomRequestHandler<T>> requestHandlers;

    final AuthenticationHandler authenticationHandler;

    final IntercomCodec<T> intercomCodec;
    private final ChannelFuture channelFuture;
    private final Channel channel;
    private EventLoopGroup bossGroup, workerGroup;

    private boolean closed = false;

    private IntercomServer(String host, int port, boolean ssl, AuthenticationHandler authenticationHandler, IntercomCodec<T> intercomCodec) throws SSLException, CertificateException {
        this.authenticationHandler = authenticationHandler;
        this.intercomCodec = intercomCodec;

        final SslContext sslCtx;
        if (ssl) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } else {
            sslCtx = null;
        }

        this.bossGroup = new EpollEventLoopGroup(1);
        this.workerGroup = new EpollEventLoopGroup();

        this.requestHandlers = new HashMap<>();

        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(EpollServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 100)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        if (sslCtx != null) {
                            p.addLast(sslCtx.newHandler(ch.alloc()));
                        }

                        p.addLast("codec", new NettyCodec());
                        p.addLast("handler", new IntercomServerHandler<>(IntercomServer.this));
                    }
                });


        // Bind and start to accept incoming connections.
        ChannelFuture bindFuture;
        if (host == null) bindFuture = b.bind(port);
        else bindFuture = b.bind(host, port);
        try {
            bindFuture = bindFuture.sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.channel = bindFuture.channel();
        this.channelFuture = bindFuture.channel().closeFuture();
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

    public void push(String event, T data) {
        if (isClosed()) throw new AlreadyClosedException("Server");

        IntercomByteBuf dataBuffer = new IntercomByteBuf(Unpooled.buffer());
        this.intercomCodec.encode(data, dataBuffer);

        this.channel.writeAndFlush(new PushPacket(event, dataBuffer));
    }

    public void addHandler(String event, IntercomRequestHandler<T> requestHandler) {
        this.requestHandlers.put(event, requestHandler);
    }

    public static class Builder {

        private String host = null;
        private int port;
        private boolean ssl = true;
        private AuthenticationHandler authenticationHandler = null;

        public Builder(int port) {
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

        public <A> Builder authenticationHandler(AuthenticationHandler<A> authenticationHandler) {
            this.authenticationHandler = authenticationHandler;
            return this;
        }

        public <T> IntercomServer<T> build(IntercomCodec<T> intercomCodec) throws SSLException, CertificateException {
            return new IntercomServer<>(host, port, ssl, authenticationHandler, intercomCodec);
        }

    }
}
