package de.maximilianbrandau.intercom.encoding.net;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;

public class ByteBufUtil {

    public static void writeUtf8(String s, ByteBuf byteBuf) {
        if (s == null) {
            byteBuf.writeInt(-1);
            return;
        }
        byteBuf.writeInt(s.length());
        byteBuf.writeBytes(s.getBytes(CharsetUtil.UTF_8));
    }

    public static String readUtf8(ByteBuf byteBuf) {
        int l = byteBuf.readInt();
        if (l == -1) return null;
        byte[] bytes = new byte[l];
        byteBuf.readBytes(bytes);
        return new String(bytes, CharsetUtil.UTF_8);
    }

}
