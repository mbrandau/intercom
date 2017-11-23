package de.maximilianbrandau.intercom;

import de.maximilianbrandau.intercom.codec.IntercomByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Assert;
import org.junit.Test;

public class ByteBufTest {

    @Test
    public void varInt() {
        int[] a = new int[]{
                Integer.MIN_VALUE,
                Integer.MIN_VALUE + 1,
                Short.MIN_VALUE - 1,
                Short.MIN_VALUE,
                Short.MIN_VALUE + 1,
                -1,
                0,
                1,
                Short.MAX_VALUE - 1,
                Short.MAX_VALUE,
                Short.MAX_VALUE + 1,
                Integer.MAX_VALUE - 1,
                Integer.MAX_VALUE
        };
        IntercomByteBuf byteBuf = new IntercomByteBuf(Unpooled.buffer(5));
        for (int i = 0; i < a.length; i++) {
            byteBuf.resetWriterIndex();
            byteBuf.resetReaderIndex();
            byteBuf.writeVarInt(a[i]);
            Assert.assertEquals(a[i], byteBuf.readVarInt());
        }
    }

    @Test
    public void utf8() {
        String s = "abc¾ÿ˫ి";
        IntercomByteBuf byteBuf = new IntercomByteBuf(Unpooled.buffer(64));
        byteBuf.writeUtf8(s);
        Assert.assertEquals(s, byteBuf.readUtf8());

        byteBuf.resetWriterIndex();
        byteBuf.resetReaderIndex();
        byteBuf.writeUtf8(null);
        Assert.assertEquals(null, byteBuf.readUtf8());
    }

}
