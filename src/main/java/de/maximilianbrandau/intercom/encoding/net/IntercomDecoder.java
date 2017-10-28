package de.maximilianbrandau.intercom.encoding.net;

import de.maximilianbrandau.intercom.encoding.net.packets.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

public class IntercomDecoder extends ReplayingDecoder<Void> {

    private static IntercomPacket decode(ByteBuf byteBuffer) {
        PacketType packetType = PacketType.getById(byteBuffer.readByte());
        int length = byteBuffer.readInt();
        ByteBuf data = byteBuffer.readSlice(length);
        IntercomPacket packet = null;
        assert packetType != null;
        switch (packetType) {
            case AUTH:
                packet = new AuthPacket();
                break;
            case AUTH_RESPONSE:
                packet = new AuthResponsePacket();
                break;
            case PING:
                packet = new PingPacket();
                break;
            case REQUEST:
                packet = new RequestPacket();
                break;
            case RESPONSE:
                packet = new ResponsePacket();
                break;
            case PUSH:
                packet = new PushPacket();
                break;
        }

        packet.decode(data);

        return packet;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        list.add(decode(byteBuf));
    }

}
