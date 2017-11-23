package de.maximilianbrandau.intercom.codec.packets;

import de.maximilianbrandau.intercom.codec.IntercomByteBuf;
import de.maximilianbrandau.intercom.codec.IntercomPacket;
import de.maximilianbrandau.intercom.codec.PacketType;

public class PingPacket extends IntercomPacket {

    private long startTime;
    private int lastPing;

    public PingPacket() {
    }

    public PingPacket(long startTime, int lastPing) {
        this.startTime = startTime;
        this.lastPing = lastPing;
    }

    public long getStartTime() {
        return startTime;
    }

    public int getLastPing() {
        return lastPing;
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.PING;
    }

    @Override
    public void encode(IntercomByteBuf byteBuffer) {
        byteBuffer.writeLong(startTime);
        byteBuffer.writeVarInt(lastPing);
    }

    @Override
    public void decode(IntercomByteBuf byteBuffer) {
        startTime = byteBuffer.readLong();
        lastPing = byteBuffer.readVarInt();
    }

}
