package de.maximilianbrandau.intercom.codec;

public interface IntercomCodec<T> {

    void encode(T data, IntercomByteBuf buffer);

    T decode(IntercomByteBuf buffer);

}
