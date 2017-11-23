package de.maximilianbrandau.intercom.codec.packets;

import de.maximilianbrandau.intercom.codec.IntercomByteBuf;
import de.maximilianbrandau.intercom.codec.IntercomPacket;
import de.maximilianbrandau.intercom.codec.PacketType;

public class RequestPacket extends IntercomPacket {

    private String requestId;
    private String event;
    private IntercomByteBuf data;

    public RequestPacket(String requestId, String event, IntercomByteBuf data) {
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

    public IntercomByteBuf getData() {
        return data;
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.REQUEST;
    }

    @Override
    public void encode(IntercomByteBuf byteBuffer) {
        byteBuffer.writeUtf8(requestId);
        byteBuffer.writeUtf8(event);
        byteBuffer.writeInt(data.writerIndex());
        byteBuffer.writeBytes(data);
    }

    @Override
    public void decode(IntercomByteBuf byteBuffer) {
        requestId = byteBuffer.readUtf8();
        event = byteBuffer.readUtf8();
        data = new IntercomByteBuf(byteBuffer.readRetainedSlice(byteBuffer.readInt()));
    }

}
