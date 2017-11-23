package de.maximilianbrandau.intercom.codec.packets;

import de.maximilianbrandau.intercom.codec.IntercomByteBuf;
import de.maximilianbrandau.intercom.codec.IntercomPacket;
import de.maximilianbrandau.intercom.codec.PacketType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class AuthPacket extends IntercomPacket {

    private final ByteBuf buffer;

    public AuthPacket(ByteBuf buffer) {
        this.buffer = buffer;
    }

    public AuthPacket() {
        this.buffer = Unpooled.buffer();
    }

    public ByteBuf getBuffer() {
        return buffer;
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.AUTH;
    }

    @Override
    public void encode(IntercomByteBuf byteBuffer) {
        byteBuffer.writeInt(buffer.writerIndex());
        byteBuffer.writeBytes(buffer);
    }

    @Override
    public void decode(IntercomByteBuf byteBuffer) {
        buffer.readBytes(byteBuffer, byteBuffer.readInt());
    }

}
