package de.maximilianbrandau.intercom.encoding;

import de.maximilianbrandau.intercom.encoding.net.ByteBufUtil;
import io.netty.buffer.ByteBuf;

public class StringEncodingMechanism implements EncodingMechanism<String> {

    @Override
    public void encode(String data, ByteBuf buffer) {
        ByteBufUtil.writeUtf8(data, buffer);
    }

    @Override
    public String decode(ByteBuf buffer) {
        return ByteBufUtil.readUtf8(buffer);
    }

}
