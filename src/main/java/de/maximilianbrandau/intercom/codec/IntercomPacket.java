package de.maximilianbrandau.intercom.codec;

public abstract class IntercomPacket {

    public abstract PacketType getPacketType();

    public abstract void encode(IntercomByteBuf byteBuffer);

    public abstract void decode(IntercomByteBuf byteBuffer);

}
