package de.maximilianbrandau.intercom.encoding.net.packets;

import de.maximilianbrandau.intercom.encoding.net.ByteBufUtil;
import de.maximilianbrandau.intercom.encoding.net.IntercomPacket;
import de.maximilianbrandau.intercom.encoding.net.PacketType;
import io.netty.buffer.ByteBuf;

public class RequestPacket extends IntercomPacket {

    private String requestId;
    private String event;
    private ByteBuf data;

    public RequestPacket(String requestId, String event, ByteBuf data) {
        this.requestId = requestId;
        this.event = event;
        this.data = data;
    }

    public RequestPacket() {
    }

    public String getRequestId() {
        return requestId;
    }

    public String getEvent() {
        return event;
    }

    public ByteBuf getData() {
        return data;
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.REQUEST;
    }

    @Override
    public void encode(ByteBuf byteBuffer) {
        ByteBufUtil.writeUtf8(requestId, byteBuffer);
        ByteBufUtil.writeUtf8(event, byteBuffer);
        byteBuffer.writeInt(data.writerIndex());
        byteBuffer.writeBytes(data);
    }

    @Override
    public void decode(ByteBuf byteBuffer) {
        requestId = ByteBufUtil.readUtf8(byteBuffer);
        event = ByteBufUtil.readUtf8(byteBuffer);
        data = byteBuffer.readRetainedSlice(byteBuffer.readInt());
    }

}
