package de.maximilianbrandau.intercom.client;

import de.maximilianbrandau.intercom.codec.packets.AuthResponsePacket;
import de.maximilianbrandau.intercom.codec.packets.PingPacket;
import de.maximilianbrandau.intercom.codec.packets.PushPacket;
import de.maximilianbrandau.intercom.codec.packets.ResponsePacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.TimeUnit;

public class IntercomClientHandler<T> extends ChannelInboundHandlerAdapter {

    private static final long PING_DELAY = 5000;

    private final IntercomClient<T> client;

    public IntercomClientHandler(IntercomClient<T> client) {
        this.client = client;
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

        }
        if (msg instanceof PingPacket) { // Pong
            // Set ping property
            this.client.ping = (int) (System.currentTimeMillis() - ((PingPacket) msg).getStartTime());
            // Schedule next ping
            this.client.eventLoopGroup.schedule(this.client::ping, PING_DELAY, TimeUnit.MILLISECONDS);
        }
        if (msg instanceof ResponsePacket) {
            ResponsePacket packet = (ResponsePacket) msg;
            SentRequest<T> sentRequest = this.client.sentRequests.get(packet.getRequestId());
            if (sentRequest != null) {
                try {
                    IntercomResponse<T> response = new IntercomResponse<>(
                            packet.getStatus(),
                            receiveTime - sentRequest.getStartTime(),
                            this.client.intercomCodec.decode(packet.getData())
                    );
                    sentRequest.getFuture().complete(response);
                } catch (Throwable t) {
                    sentRequest.getFuture().completeExceptionally(t);
                } finally {
                    sentRequest.getTimeoutFuture().cancel(false);
                    this.client.sentRequests.remove(packet.getRequestId());
                }
            }
        }
        if (msg instanceof PushPacket) {
            PushPacket packet = (PushPacket) msg;
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
        this.client.connect();
    }

}
