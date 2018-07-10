package de.maximilianbrandau.intercom.codec.packets;

import de.maximilianbrandau.intercom.codec.IntercomByteBuf;
import de.maximilianbrandau.intercom.codec.IntercomPacket;
import de.maximilianbrandau.intercom.codec.PacketType;

public class AuthPacket extends IntercomPacket {

    private IntercomByteBuf data;

    public AuthPacket(IntercomByteBuf data) {
        this.data = data;
    }

    public AuthPacket() {
    }

    public IntercomByteBuf getData() {
        return data;
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.AUTH;
    }

    @Override
    public void encode(IntercomByteBuf byteBuffer) {
        byteBuffer.writeInt(data.writerIndex());
        byteBuffer.writeBytes(data);
    }

    @Override
    public void decode(IntercomByteBuf byteBuffer) {
        data = new IntercomByteBuf(byteBuffer.readRetainedSlice(byteBuffer.readInt()));
    }

}
