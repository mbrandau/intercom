package de.maximilianbrandau.intercom.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class IntercomEncoder extends MessageToByteEncoder<IntercomPacket> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, IntercomPacket intercomPacket, ByteBuf byteBuf) throws Exception {
        ByteBuf dataBuffer = Unpooled.buffer();
        intercomPacket.encode(new IntercomByteBuf(dataBuffer));

        byteBuf.writeByte(intercomPacket.getPacketType().getId());
        byteBuf.writeInt(dataBuffer.writerIndex());
        byteBuf.writeBytes(dataBuffer);
    }

}
