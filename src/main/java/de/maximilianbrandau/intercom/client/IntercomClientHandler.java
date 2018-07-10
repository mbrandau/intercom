package de.maximilianbrandau.intercom.client;

import de.maximilianbrandau.intercom.Event;
import de.maximilianbrandau.intercom.IntercomRequestHandler;
import de.maximilianbrandau.intercom.OutgoingResponse;
import de.maximilianbrandau.intercom.Request;
import de.maximilianbrandau.intercom.codec.packets.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public class IntercomClientHandler<T, A> extends ChannelInboundHandlerAdapter {

    private static final long PING_DELAY = 5000;

    private final IntercomClient<T, A> client;
    private final EventHandler<T> eventHandler;

    public IntercomClientHandler(IntercomClient<T, A> client, EventHandler<T> eventHandler) {
        this.client = client;
        this.eventHandler = eventHandler;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        // Start pinging
        this.client.ping();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        long receiveTime = System.currentTimeMillis();
        if (msg instanceof AuthResponsePacket) {
            this.client.handleAuthenticationResponse((AuthResponsePacket) msg);
        } else if (msg instanceof PingPacket) { // Pong
            // Set ping property
            this.client.ping = (int) (System.currentTimeMillis() - ((PingPacket) msg).getStartTime());
            // Schedule next ping
            this.client.eventLoopGroup.schedule(this.client::ping, PING_DELAY, TimeUnit.MILLISECONDS);
        } else if (msg instanceof ResponsePacket) {
            ResponsePacket packet = (ResponsePacket) msg;
            this.client.getRequestFactory().handleResponse(packet, receiveTime);
        } else if (msg instanceof PushPacket) {
            PushPacket packet = (PushPacket) msg;
            T data = this.client.codec.decode(packet.getData());
            this.eventHandler.handleEvent(new Event<>(packet.getEvent(), data));
        } else if (msg instanceof RequestPacket) {
            RequestPacket packet = (RequestPacket) msg;
            IntercomRequestHandler<T> handler = this.client.getRequestHandlerRegistry().getHandlerOrDefault(packet.getRoute());
            if (handler != null) {
                Request<T> request = new Request<>(
                        System.currentTimeMillis(),
                        (InetSocketAddress) ctx.channel().remoteAddress(),
                        this.client.authenticationResult,
                        packet.getRequestId(),
                        packet.getRoute(),
                        this.client.codec.decode(packet.getData())
                );
                OutgoingResponse<T> response = new OutgoingResponse<>(this.client.codec, ctx, request);

                handler.handleRequest(request, response);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        // Reconnect
        if (!this.client.isClosing() && !this.client.isClosed()) this.client.connect();
    }

}
