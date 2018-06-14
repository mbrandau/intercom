package de.maximilianbrandau.intercom.codec.defaults;

import de.maximilianbrandau.intercom.codec.IntercomByteBuf;
import de.maximilianbrandau.intercom.codec.IntercomCodec;

public class IntercomStringCodec extends IntercomCodec<String> {

    @Override
    public void encode(String data, IntercomByteBuf buffer) {
        buffer.writeUtf8(data);
    }

    @Override
    public String decode(IntercomByteBuf buffer) {
        return buffer.readUtf8();
    }

}
