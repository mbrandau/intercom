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
