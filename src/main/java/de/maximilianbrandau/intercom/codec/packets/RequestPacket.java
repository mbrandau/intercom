package de.maximilianbrandau.intercom.codec.packets;

import de.maximilianbrandau.intercom.codec.IntercomByteBuf;
import de.maximilianbrandau.intercom.codec.IntercomPacket;
import de.maximilianbrandau.intercom.codec.PacketType;

public class RequestPacket extends IntercomPacket {

    private int requestId;
    private String route;
    private IntercomByteBuf data;

    public RequestPacket(int requestId, String route, IntercomByteBuf data) {
        this.requestId = requestId;
        this.route = route;
        this.data = data;
    }

    public RequestPacket() {
    }

    public int getRequestId() {
        return requestId;
    }

    public String getRoute() {
        return route;
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
        byteBuffer.writeVarInt(requestId);
        byteBuffer.writeUtf8(route);
        byteBuffer.writeInt(data.writerIndex());
        byteBuffer.writeBytes(data);
    }

    @Override
    public void decode(IntercomByteBuf byteBuffer) {
        requestId = byteBuffer.readVarInt();
        route = byteBuffer.readUtf8();
        data = new IntercomByteBuf(byteBuffer.readRetainedSlice(byteBuffer.readInt()));
    }

}
