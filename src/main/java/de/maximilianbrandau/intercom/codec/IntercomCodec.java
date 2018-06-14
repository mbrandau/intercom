package de.maximilianbrandau.intercom.codec;

public abstract class IntercomCodec<T> {


    public abstract void encode(T data, IntercomByteBuf buffer);

    public abstract T decode(IntercomByteBuf buffer);

}
