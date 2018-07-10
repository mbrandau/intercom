package de.maximilianbrandau.intercom.codec.packets;

import de.maximilianbrandau.intercom.codec.IntercomByteBuf;
import de.maximilianbrandau.intercom.codec.IntercomPacket;
import de.maximilianbrandau.intercom.codec.PacketType;

public class ResponsePacket extends IntercomPacket {

    private int requestId;
    private short status;
    private IntercomByteBuf data;

    public ResponsePacket(int requestId, short status, IntercomByteBuf data) {
        this.requestId = requestId;
        this.status = status;
        this.data = data;
    }

    public ResponsePacket() {
    }

    public int getRequestId() {
        return requestId;
    }

    public short getStatus() {
        return status;
    }

    public IntercomByteBuf getData() {
        return data;
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.RESPONSE;
    }

    @Override
    public void encode(IntercomByteBuf byteBuffer) {
        byteBuffer.writeVarInt(requestId);
        byteBuffer.writeShort(status);
        byteBuffer.writeInt(data.writerIndex());
        byteBuffer.writeBytes(data);
    }

    @Override
    public void decode(IntercomByteBuf byteBuffer) {
        requestId = byteBuffer.readVarInt();
        status = byteBuffer.readShort();
        data = new IntercomByteBuf(byteBuffer.readRetainedSlice(byteBuffer.readInt()));
    }

}
