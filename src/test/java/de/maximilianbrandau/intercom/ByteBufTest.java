/*
 * Copyright (c) 2017-2018 Maximilian Brandau
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

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
        for (int anA : a) {
            byteBuf.resetReaderIndex();
            byteBuf.resetWriterIndex();
            byteBuf.writeVarInt(anA);
            Assert.assertEquals(anA, byteBuf.readVarInt());
        }
    }

    @Test
    public void utf8() {
        String s = "abc¾ÿ˫ి";
        IntercomByteBuf byteBuf = new IntercomByteBuf(Unpooled.buffer(64));
        byteBuf.writeUtf8(s);
        Assert.assertEquals(s, byteBuf.readUtf8());

        byteBuf.resetReaderIndex();
        byteBuf.resetWriterIndex();
        byteBuf.writeUtf8(null);
        Assert.assertNull(byteBuf.readUtf8());
    }

}
