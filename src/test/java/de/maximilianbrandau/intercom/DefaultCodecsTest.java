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
import de.maximilianbrandau.intercom.codec.IntercomCodec;
import de.maximilianbrandau.intercom.codec.defaults.IntercomBsonCodec;
import de.maximilianbrandau.intercom.codec.defaults.IntercomStringCodec;
import io.netty.buffer.Unpooled;
import org.bson.Document;
import org.junit.Assert;
import org.junit.Test;

public class DefaultCodecsTest {

    @Test
    public void intercomBsonCodec() {
        IntercomCodec<Document> codec = new IntercomBsonCodec();
        IntercomByteBuf buffer = new IntercomByteBuf(Unpooled.buffer());

        codec.encode(new Document("test", "This is a test!"), buffer);

        Document document = codec.decode(buffer);
        Assert.assertEquals("This is a test!", document.getString("test"));
    }

    @Test
    public void intercomStringCodec() {
        IntercomCodec<String> codec = new IntercomStringCodec();
        IntercomByteBuf buffer = new IntercomByteBuf(Unpooled.buffer());

        codec.encode("This is a test!", buffer);

        String decoded = codec.decode(buffer);
        Assert.assertEquals("This is a test!", decoded);
    }

}
