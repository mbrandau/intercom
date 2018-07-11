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

package de.maximilianbrandau.intercom.server;

import de.maximilianbrandau.intercom.authentication.AuthenticationHandler;
import de.maximilianbrandau.intercom.authentication.AuthenticationResult;
import de.maximilianbrandau.intercom.codec.NettyCodec;
import de.maximilianbrandau.intercom.codec.packets.AuthPacket;
import de.maximilianbrandau.intercom.codec.packets.AuthResponsePacket;
import de.maximilianbrandau.intercom.codec.packets.PingPacket;
import de.maximilianbrandau.intercom.codec.packets.RequestPacket;
import de.maximilianbrandau.intercom.requests.IntercomRequestHandler;
import de.maximilianbrandau.intercom.requests.OutgoingResponse;
import de.maximilianbrandau.intercom.requests.Request;
import de.maximilianbrandau.intercom.requests.RequestFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.util.ReferenceCountUtil;

import java.net.InetSocketAddress;

public class Client<T, A> {

    private final RequestFactory<T> requestFactory;
    private final IntercomServer<T, A, ?> server;
    private final Channel channel;
    private AuthenticationResult authenticationResult;

    public Client(IntercomServer<T, A, ?> server, Channel channel) {
        this.server = server;
        this.channel = channel;

        this.requestFactory = new RequestFactory<>(server.codec, server.workerGroup, server.requestTimeout);

        ChannelPipeline p = channel.pipeline();

        p.addLast("codec", new NettyCodec());
        p.addLast("handler", new IntercomServerHandler());
    }

    public RequestFactory<T>.Builder request(String route) {
        return requestFactory.newBuilder(route, this.channel);
    }

    private class IntercomServerHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            try {
                if (msg instanceof PingPacket) {
                    ctx.writeAndFlush(msg);
                } else if (msg instanceof AuthPacket) {
                    AuthPacket packet = (AuthPacket) msg;
                    AuthenticationHandler<A> authenticationHandler = Client.this.server.authenticationHandler;
                    if (authenticationHandler != null) {
                        authenticationResult = authenticationHandler.authenticate(Client.this.server.authenticationCodec.decode(packet.getData()));
                        ctx.writeAndFlush(new AuthResponsePacket(authenticationResult));
                    }
                } else if (msg instanceof RequestPacket) {
                    RequestPacket packet = (RequestPacket) msg;
                    IntercomRequestHandler<T> handler = Client.this.server.getRequestHandlerRegistry().getHandlerOrDefault(packet.getRoute());
                    if (handler != null) {
                        Request<T> request = new Request<>(
                                System.currentTimeMillis(),
                                (InetSocketAddress) ctx.channel().remoteAddress(),
                                authenticationResult,
                                packet.getRequestId(),
                                packet.getRoute(),
                                Client.this.server.codec.decode(packet.getData())
                        );
                        OutgoingResponse<T> response = new OutgoingResponse<>(Client.this.server.codec, ctx, request);

                        handler.handleRequest(request, response);
                    }
                }
            } finally {
                // Releasing the data
                ReferenceCountUtil.release(msg);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
        }
    }
}
