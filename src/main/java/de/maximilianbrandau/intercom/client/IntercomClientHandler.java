/*
 * Copyright (c) 2017-2018 Maximilian Brandau
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package de.maximilianbrandau.intercom.client;

import de.maximilianbrandau.intercom.Event;
import de.maximilianbrandau.intercom.codec.packets.*;
import de.maximilianbrandau.intercom.requests.IntercomRequestHandler;
import de.maximilianbrandau.intercom.requests.OutgoingResponse;
import de.maximilianbrandau.intercom.requests.Request;
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
