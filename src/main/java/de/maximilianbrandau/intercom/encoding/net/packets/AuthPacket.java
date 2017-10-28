package de.maximilianbrandau.intercom.encoding.net.packets;

import de.maximilianbrandau.intercom.encoding.net.IntercomPacket;
import de.maximilianbrandau.intercom.encoding.net.PacketType;
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
    public void encode(ByteBuf byteBuffer) {
        byteBuffer.writeInt(buffer.writerIndex());
        byteBuffer.writeBytes(buffer);
    }

    @Override
    public void decode(ByteBuf byteBuffer) {
        buffer.readBytes(byteBuffer, byteBuffer.readInt());
    }

}
