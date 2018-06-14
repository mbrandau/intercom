package de.maximilianbrandau.intercom.codec;

import de.maximilianbrandau.intercom.codec.packets.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

public class NettyCodec extends ByteToMessageCodec<IntercomPacket> {

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

        packet.decode(new IntercomByteBuf(data));

        return packet;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, IntercomPacket intercomPacket, ByteBuf byteBuf) {
        ByteBuf dataBuffer = Unpooled.buffer();
        intercomPacket.encode(new IntercomByteBuf(dataBuffer));

        byteBuf.writeByte(intercomPacket.getPacketType().getId());
        byteBuf.writeInt(dataBuffer.writerIndex());
        byteBuf.writeBytes(dataBuffer);
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) {
        list.add(decode(byteBuf));
    }
}
