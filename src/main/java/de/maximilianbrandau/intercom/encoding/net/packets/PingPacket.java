package de.maximilianbrandau.intercom.encoding.net.packets;

import de.maximilianbrandau.intercom.encoding.net.IntercomPacket;
import de.maximilianbrandau.intercom.encoding.net.PacketType;
import io.netty.buffer.ByteBuf;

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
    public void encode(ByteBuf byteBuffer) {
        byteBuffer.writeLong(startTime);
        byteBuffer.writeInt(lastPing);
    }

    @Override
    public void decode(ByteBuf byteBuffer) {
        startTime = byteBuffer.readLong();
        lastPing = byteBuffer.readInt();
    }

}
