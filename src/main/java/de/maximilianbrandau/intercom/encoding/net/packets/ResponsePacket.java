package de.maximilianbrandau.intercom.encoding.net.packets;

import de.maximilianbrandau.intercom.encoding.net.ByteBufUtil;
import de.maximilianbrandau.intercom.encoding.net.IntercomPacket;
import de.maximilianbrandau.intercom.encoding.net.PacketType;
import io.netty.buffer.ByteBuf;

public class ResponsePacket extends IntercomPacket {

    private String requestId;
    private short status;
    private ByteBuf data;

    public ResponsePacket(String requestId, short status, ByteBuf data) {
        this.requestId = requestId;
        this.status = status;
        this.data = data;
    }

    public ResponsePacket() {
    }

    public String getRequestId() {
        return requestId;
    }

    public short getStatus() {
        return status;
    }

    public ByteBuf getData() {
        return data;
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.RESPONSE;
    }

    @Override
    public void encode(ByteBuf byteBuffer) {
        ByteBufUtil.writeUtf8(requestId, byteBuffer);
        byteBuffer.writeShort(status);
        byteBuffer.writeInt(data.writerIndex());
        byteBuffer.writeBytes(data);
    }

    @Override
    public void decode(ByteBuf byteBuffer) {
        requestId = ByteBufUtil.readUtf8(byteBuffer);
        status = byteBuffer.readShort();
        data = byteBuffer.readRetainedSlice(byteBuffer.readInt());
    }

}
