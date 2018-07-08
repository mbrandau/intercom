package de.maximilianbrandau.intercom.codec.defaults;

import de.maximilianbrandau.intercom.codec.IntercomByteBuf;
import de.maximilianbrandau.intercom.codec.IntercomCodec;
import org.bson.BsonBinaryReader;
import org.bson.BsonBinaryWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.EncoderContext;
import org.bson.io.BasicOutputBuffer;

import java.nio.ByteBuffer;

public class IntercomBsonCodec implements IntercomCodec<Document> {

    private final static Codec<Document> DOCUMENT_CODEC = new DocumentCodec();

    @Override
    public void encode(Document data, IntercomByteBuf buffer) {
        BasicOutputBuffer outputBuffer = new BasicOutputBuffer();
        BsonBinaryWriter writer = new BsonBinaryWriter(outputBuffer);
        DOCUMENT_CODEC.encode(writer, data, EncoderContext.builder().isEncodingCollectibleDocument(true).build());

        byte[] bytes = outputBuffer.toByteArray();
        buffer.writeInt(bytes.length);
        buffer.writeBytes(bytes);
    }

    @Override
    public Document decode(IntercomByteBuf buffer) {
        int length = buffer.readInt();
        byte[] bytes = new byte[length];
        buffer.readBytes(bytes);

        BsonBinaryReader bsonReader = new BsonBinaryReader(ByteBuffer.wrap(bytes));
        return DOCUMENT_CODEC.decode(bsonReader, DecoderContext.builder().build());
    }
}
