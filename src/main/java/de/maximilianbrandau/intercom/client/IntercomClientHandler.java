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
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // Start pinging
        this.client.ping();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        long receiveTime = System.currentTimeMillis();
        if (msg instanceof AuthResponsePacket) {

        }
        if (msg instanceof PingPacket) { // Pong
            int ping = (int) (System.currentTimeMillis() - ((PingPacket) msg).getStartTime());
            // Set ping property
            this.client.ping = ping;
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
                            this.client.encodingMechanism.decode(packet.getData())
                    );
                    sentRequest.getResponseHandler().handleResponse(response);
                } catch (Throwable t) {
                    sentRequest.getResponseHandler().handleError(t);
                } finally {
                    sentRequest.getFuture().cancel(false);
                    this.client.sentRequests.remove(packet.getRequestId());
                }
            }
        }
        if (msg instanceof PushPacket) {
            PushPacket packet = (PushPacket) msg;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // Reconnect
        this.client.connect();
    }

}
