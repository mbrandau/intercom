package de.maximilianbrandau.intercom.encoding.net.packets;

import de.maximilianbrandau.intercom.encoding.net.ByteBufUtil;
import de.maximilianbrandau.intercom.encoding.net.IntercomPacket;
import de.maximilianbrandau.intercom.encoding.net.PacketType;
import io.netty.buffer.ByteBuf;

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
    public void encode(ByteBuf byteBuffer) {
        ByteBufUtil.writeUtf8(event, byteBuffer);
    }

    @Override
    public void decode(ByteBuf byteBuffer) {
        event = ByteBufUtil.readUtf8(byteBuffer);
    }

}
