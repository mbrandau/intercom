package de.maximilianbrandau.intercom.encoding.net;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;

public class ByteBufUtil {

    public static void writeUtf8(String s, ByteBuf byteBuf) {
        byteBuf.writeInt(s.length());
        byteBuf.writeBytes(s.getBytes(CharsetUtil.UTF_8));
    }

    public static String readUtf8(ByteBuf byteBuf) {
        byte[] bytes = new byte[byteBuf.readInt()];
        byteBuf.readBytes(bytes);
        return new String(bytes, CharsetUtil.UTF_8);
    }

}
