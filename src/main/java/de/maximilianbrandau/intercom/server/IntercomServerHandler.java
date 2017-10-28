package de.maximilianbrandau.intercom.server;

import de.maximilianbrandau.intercom.AuthenticationHandler;
import de.maximilianbrandau.intercom.encoding.net.packets.AuthPacket;
import de.maximilianbrandau.intercom.encoding.net.packets.PingPacket;
import de.maximilianbrandau.intercom.encoding.net.packets.RequestPacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.net.InetSocketAddress;

public class IntercomServerHandler<T> extends ChannelInboundHandlerAdapter {

    private final IntercomServer<T> server;
    private boolean authenticated = false;

    public IntercomServerHandler(IntercomServer<T> server) {
        this.server = server;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof PingPacket) {
                ctx.writeAndFlush(msg);
                return;
            }
            if (msg instanceof AuthPacket) {
                AuthPacket packet = (AuthPacket) msg;
                AuthenticationHandler authenticationHandler = this.server.authenticationHandler;
                if (authenticationHandler != null)
                    this.authenticated = authenticationHandler.authenticate(this.server.authenticationHandler.decode(packet.getBuffer()));
                return;
            }
            if (msg instanceof RequestPacket) {
                RequestPacket packet = (RequestPacket) msg;
                IntercomRequestHandler<T> handler = this.server.requestHandlers.get(packet.getEvent());
                if (handler != null) {
                    IntercomRequest<T> request = new IntercomRequest<>(
                            System.currentTimeMillis(),
                            (InetSocketAddress) ctx.channel().remoteAddress(),
                            this.authenticated,
                            packet.getRequestId(),
                            packet.getEvent(),
                            this.server.encodingMechanism.decode(packet.getData())
                    );
                    IntercomResponse<T> response = new IntercomResponse<>(this.server, ctx, request);

                    handler.handleRequest(request, response);
                }
                return;
            }
        } finally {
            // Releasing the data
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    }
}
