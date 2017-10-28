package de.maximilianbrandau.intercom.encoding.net;

import io.netty.buffer.ByteBuf;

public abstract class IntercomPacket {

    public abstract PacketType getPacketType();

    public abstract void encode(ByteBuf byteBuffer);

    public abstract void decode(ByteBuf byteBuffer);

}
