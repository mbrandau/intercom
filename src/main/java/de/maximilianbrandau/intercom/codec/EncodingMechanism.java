package de.maximilianbrandau.intercom.codec;

public interface EncodingMechanism<T> {

    void encode(T data, IntercomByteBuf buffer);

    T decode(IntercomByteBuf buffer);

}
