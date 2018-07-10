package de.maximilianbrandau.intercom.codec.packets;

import de.maximilianbrandau.intercom.authentication.AuthenticationResult;
import de.maximilianbrandau.intercom.codec.IntercomByteBuf;
import de.maximilianbrandau.intercom.codec.IntercomPacket;
import de.maximilianbrandau.intercom.codec.PacketType;

public class AuthResponsePacket extends IntercomPacket {

    private AuthenticationResult result;

    public AuthResponsePacket(AuthenticationResult result) {
        this.result = result;
    }

    public AuthResponsePacket() {
    }

    public AuthenticationResult getResult() {
        return result;
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.AUTH_RESPONSE;
    }

    @Override
    public void encode(IntercomByteBuf byteBuffer) {
        byteBuffer.writeUtf8(getResult().getError());
    }

    @Override
    public void decode(IntercomByteBuf byteBuffer) {
        this.result = AuthenticationResult.failure(byteBuffer.readUtf8());
    }

}
