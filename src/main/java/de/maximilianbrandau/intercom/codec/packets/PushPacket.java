package de.maximilianbrandau.intercom.codec.packets;

import de.maximilianbrandau.intercom.codec.IntercomByteBuf;
import de.maximilianbrandau.intercom.codec.IntercomPacket;
import de.maximilianbrandau.intercom.codec.PacketType;

public class PushPacket extends IntercomPacket {

    private String event;

    public PushPacket(String event) {
        this.event = event;
    }

    public PushPacket() {
    }

    public String getEvent() {
        return event;
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.PUSH;
    }

    @Override
    public void encode(IntercomByteBuf byteBuffer) {
        byteBuffer.writeUtf8(event);
    }

    @Override
    public void decode(IntercomByteBuf byteBuffer) {
        event = byteBuffer.readUtf8();
    }

}
