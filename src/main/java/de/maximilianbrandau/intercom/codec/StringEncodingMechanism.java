package de.maximilianbrandau.intercom.codec;

public class StringEncodingMechanism implements EncodingMechanism<String> {

    @Override
    public void encode(String data, IntercomByteBuf buffer) {
        buffer.writeUtf8(data);
    }

    @Override
    public String decode(IntercomByteBuf buffer) {
        return buffer.readUtf8();
    }

}
