package de.maximilianbrandau.intercom.codec.packets;

import de.maximilianbrandau.intercom.codec.IntercomByteBuf;
import de.maximilianbrandau.intercom.codec.IntercomPacket;
import de.maximilianbrandau.intercom.codec.PacketType;

public class PushPacket extends IntercomPacket {

    private String event;
    private IntercomByteBuf data;

    public PushPacket(String event, IntercomByteBuf data) {
        this.event = event;
        this.data = data;
    }

    public PushPacket() {
    }

    public String getEvent() {
        return event;
    }

    public IntercomByteBuf getData() {
        return data;
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.PUSH;
    }

    @Override
    public void encode(IntercomByteBuf byteBuffer) {
        byteBuffer.writeUtf8(event);
        byteBuffer.writeInt(data.writerIndex());
        byteBuffer.writeBytes(data);
    }

    @Override
    public void decode(IntercomByteBuf byteBuffer) {
        event = byteBuffer.readUtf8();
        data = new IntercomByteBuf(byteBuffer.readRetainedSlice(byteBuffer.readInt()));
    }

}
