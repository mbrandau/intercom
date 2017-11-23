package de.maximilianbrandau.intercom.codec;

import io.netty.buffer.ByteBuf;

public interface AuthenticationEncodingMechanism<T> {

    void encode(T data, ByteBuf buffer);

    T decode(ByteBuf buffer);

}
