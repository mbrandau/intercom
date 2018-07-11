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

package de.maximilianbrandau.intercom.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.util.ByteProcessor;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;

public class IntercomByteBuf extends ByteBuf {

    private final ByteBuf byteBuf;

    public IntercomByteBuf(ByteBuf byteBuf) {
        this.byteBuf = byteBuf;
    }

    public void writeVarInt(int i) {
        while ((i & 0b11111111111111111111111110000000) != 0) {
            this.writeByte(i & 0b1111111 | 0b10000000);
            i >>>= 7;
        }
        this.writeByte(i);
    }

    public int readVarInt() {
        int i = 0, byteCount = 0;
        byte b;
        do {
            b = this.readByte();
            i |= (b & 0b1111111) << byteCount++ * 7;
            if (byteCount > 5) throw new RuntimeException("VarInt too big");
        } while ((b & 0b10000000) == 0b10000000);
        return i;
    }

    public void writeUtf8(String string) {
        if (string == null) {
            this.writeVarInt(-1);
            return;
        }
        byte[] bytes = string.getBytes(CharsetUtil.UTF_8);
        this.writeVarInt(bytes.length);
        this.writeBytes(bytes);
    }

    public String readUtf8() {
        int length = this.readVarInt();
        if (length == -1) return null;
        byte[] bytes = new byte[length];
        this.readBytes(bytes);
        return new String(bytes, CharsetUtil.UTF_8);
    }

    public void writeEnum(Enum<?> e) {
        byteBuf.writeInt(e.ordinal());
    }

    public <E extends Enum> E readEnum(Class<E> enumClass) {
        return enumClass.getEnumConstants()[byteBuf.readInt()];
    }

    @Override
    public int capacity() {
        return byteBuf.capacity();
    }

    @Override
    public ByteBuf capacity(int i) {
        return byteBuf.capacity(i);
    }

    @Override
    public int maxCapacity() {
        return byteBuf.maxCapacity();
    }

    @Override
    public ByteBufAllocator alloc() {
        return byteBuf.alloc();
    }

    @Override
    public ByteOrder order() {
        return byteBuf.order();
    }

    @Override
    public ByteBuf order(ByteOrder byteorder) {
        return byteBuf.order(byteorder);
    }

    @Override
    public ByteBuf unwrap() {
        return byteBuf.unwrap();
    }

    @Override
    public boolean isDirect() {
        return byteBuf.isDirect();
    }

    @Override
    public int readerIndex() {
        return byteBuf.readerIndex();
    }

    @Override
    public ByteBuf readerIndex(int i) {
        return byteBuf.readerIndex(i);
    }

    @Override
    public int writerIndex() {
        return byteBuf.writerIndex();
    }

    @Override
    public ByteBuf writerIndex(int i) {
        return byteBuf.writerIndex(i);
    }

    @Override
    public ByteBuf setIndex(int i, int j) {
        return byteBuf.setIndex(i, j);
    }

    @Override
    public int readableBytes() {
        return byteBuf.readableBytes();
    }

    @Override
    public int writableBytes() {
        return byteBuf.writableBytes();
    }

    @Override
    public int maxWritableBytes() {
        return byteBuf.maxWritableBytes();
    }

    @Override
    public boolean isReadable() {
        return byteBuf.isReadable();
    }

    @Override
    public boolean isReadable(int i) {
        return byteBuf.isReadable(i);
    }

    @Override
    public boolean isWritable() {
        return byteBuf.isWritable();
    }

    @Override
    public boolean isWritable(int i) {
        return byteBuf.isWritable(i);
    }

    @Override
    public ByteBuf clear() {
        return byteBuf.clear();
    }

    @Override
    public ByteBuf markReaderIndex() {
        return byteBuf.markReaderIndex();
    }

    @Override
    public ByteBuf resetReaderIndex() {
        return byteBuf.resetReaderIndex();
    }

    @Override
    public ByteBuf markWriterIndex() {
        return byteBuf.markWriterIndex();
    }

    @Override
    public ByteBuf resetWriterIndex() {
        return byteBuf.resetWriterIndex();
    }

    @Override
    public ByteBuf discardReadBytes() {
        return byteBuf.discardReadBytes();
    }

    @Override
    public ByteBuf discardSomeReadBytes() {
        return byteBuf.discardSomeReadBytes();
    }

    @Override
    public ByteBuf ensureWritable(int i) {
        return byteBuf.ensureWritable(i);
    }

    @Override
    public int ensureWritable(int i, boolean flag) {
        return byteBuf.ensureWritable(i, flag);
    }

    @Override
    public boolean getBoolean(int i) {
        return byteBuf.getBoolean(i);
    }

    @Override
    public byte getByte(int i) {
        return byteBuf.getByte(i);
    }

    @Override
    public short getUnsignedByte(int i) {
        return byteBuf.getUnsignedByte(i);
    }

    @Override
    public short getShort(int i) {
        return byteBuf.getShort(i);
    }

    @Override
    public int getUnsignedShort(int i) {
        return byteBuf.getUnsignedShort(i);
    }

    @Override
    public int getMedium(int i) {
        return byteBuf.getMedium(i);
    }

    @Override
    public int getUnsignedMedium(int i) {
        return byteBuf.getUnsignedMedium(i);
    }

    @Override
    public int getInt(int i) {
        return byteBuf.getInt(i);
    }

    @Override
    public long getUnsignedInt(int i) {
        return byteBuf.getUnsignedInt(i);
    }

    @Override
    public long getLong(int i) {
        return byteBuf.getLong(i);
    }

    @Override
    public char getChar(int i) {
        return byteBuf.getChar(i);
    }

    @Override
    public float getFloat(int i) {
        return byteBuf.getFloat(i);
    }

    @Override
    public double getDouble(int i) {
        return byteBuf.getDouble(i);
    }

    @Override
    public ByteBuf getBytes(int i, ByteBuf bytebuf) {
        return byteBuf.getBytes(i, bytebuf);
    }

    @Override
    public ByteBuf getBytes(int i, ByteBuf bytebuf, int j) {
        return byteBuf.getBytes(i, bytebuf, j);
    }

    @Override
    public ByteBuf getBytes(int i, ByteBuf bytebuf, int j, int k) {
        return byteBuf.getBytes(i, bytebuf, j, k);
    }

    @Override
    public ByteBuf getBytes(int i, byte abyte[]) {
        return byteBuf.getBytes(i, abyte);
    }

    @Override
    public ByteBuf getBytes(int i, byte abyte[], int j, int k) {
        return byteBuf.getBytes(i, abyte, j, k);
    }

    @Override
    public ByteBuf getBytes(int i, ByteBuffer bytebuffer) {
        return byteBuf.getBytes(i, bytebuffer);
    }

    @Override
    public ByteBuf getBytes(int i, OutputStream outputstream, int j) throws IOException {
        return byteBuf.getBytes(i, outputstream, j);
    }

    @Override
    public int getBytes(int i, GatheringByteChannel gatheringbytechannel, int j) throws IOException {
        return byteBuf.getBytes(i, gatheringbytechannel, j);
    }

    @Override
    public ByteBuf setBoolean(int i, boolean flag) {
        return byteBuf.setBoolean(i, flag);
    }

    @Override
    public ByteBuf setByte(int i, int j) {
        return byteBuf.setByte(i, j);
    }

    @Override
    public ByteBuf setShort(int i, int j) {
        return byteBuf.setShort(i, j);
    }

    @Override
    public ByteBuf setMedium(int i, int j) {
        return byteBuf.setMedium(i, j);
    }

    @Override
    public ByteBuf setInt(int i, int j) {
        return byteBuf.setInt(i, j);
    }

    @Override
    public ByteBuf setLong(int i, long j) {
        return byteBuf.setLong(i, j);
    }

    @Override
    public ByteBuf setChar(int i, int j) {
        return byteBuf.setChar(i, j);
    }

    @Override
    public ByteBuf setFloat(int i, float f) {
        return byteBuf.setFloat(i, f);
    }

    @Override
    public ByteBuf setDouble(int i, double d0) {
        return byteBuf.setDouble(i, d0);
    }

    @Override
    public ByteBuf setBytes(int i, ByteBuf bytebuf) {
        return byteBuf.setBytes(i, bytebuf);
    }

    @Override
    public ByteBuf setBytes(int i, ByteBuf bytebuf, int j) {
        return byteBuf.setBytes(i, bytebuf, j);
    }

    @Override
    public ByteBuf setBytes(int i, ByteBuf bytebuf, int j, int k) {
        return byteBuf.setBytes(i, bytebuf, j, k);
    }

    @Override
    public ByteBuf setBytes(int i, byte abyte[]) {
        return byteBuf.setBytes(i, abyte);
    }

    @Override
    public ByteBuf setBytes(int i, byte abyte[], int j, int k) {
        return byteBuf.setBytes(i, abyte, j, k);
    }

    @Override
    public ByteBuf setBytes(int i, ByteBuffer bytebuffer) {
        return byteBuf.setBytes(i, bytebuffer);
    }

    @Override
    public int setBytes(int i, InputStream inputstream, int j) throws IOException {
        return byteBuf.setBytes(i, inputstream, j);
    }

    @Override
    public int setBytes(int i, ScatteringByteChannel scatteringbytechannel, int j) throws IOException {
        return byteBuf.setBytes(i, scatteringbytechannel, j);
    }

    @Override
    public ByteBuf setZero(int i, int j) {
        return byteBuf.setZero(i, j);
    }

    @Override
    public boolean readBoolean() {
        return byteBuf.readBoolean();
    }

    @Override
    public byte readByte() {
        return byteBuf.readByte();
    }

    @Override
    public short readUnsignedByte() {
        return byteBuf.readUnsignedByte();
    }

    @Override
    public short readShort() {
        return byteBuf.readShort();
    }

    @Override
    public int readUnsignedShort() {
        return byteBuf.readUnsignedShort();
    }

    @Override
    public int readMedium() {
        return byteBuf.readMedium();
    }

    @Override
    public int readUnsignedMedium() {
        return byteBuf.readUnsignedMedium();
    }

    @Override
    public int readInt() {
        return byteBuf.readInt();
    }

    @Override
    public long readUnsignedInt() {
        return byteBuf.readUnsignedInt();
    }

    @Override
    public long readLong() {
        return byteBuf.readLong();
    }

    @Override
    public char readChar() {
        return byteBuf.readChar();
    }

    @Override
    public float readFloat() {
        return byteBuf.readFloat();
    }

    @Override
    public double readDouble() {
        return byteBuf.readDouble();
    }

    @Override
    public ByteBuf readBytes(int i) {
        return byteBuf.readBytes(i);
    }

    @Override
    public ByteBuf readSlice(int i) {
        return byteBuf.readSlice(i);
    }

    @Override
    public ByteBuf readBytes(ByteBuf bytebuf) {
        return byteBuf.readBytes(bytebuf);
    }

    @Override
    public ByteBuf readBytes(ByteBuf bytebuf, int i) {
        return byteBuf.readBytes(bytebuf, i);
    }

    @Override
    public ByteBuf readBytes(ByteBuf bytebuf, int i, int j) {
        return byteBuf.readBytes(bytebuf, i, j);
    }

    @Override
    public ByteBuf readBytes(byte abyte[]) {
        return byteBuf.readBytes(abyte);
    }

    @Override
    public ByteBuf readBytes(byte abyte[], int i, int j) {
        return byteBuf.readBytes(abyte, i, j);
    }

    @Override
    public ByteBuf readBytes(ByteBuffer bytebuffer) {
        return byteBuf.readBytes(bytebuffer);
    }

    @Override
    public ByteBuf readBytes(OutputStream outputstream, int i) throws IOException {
        return byteBuf.readBytes(outputstream, i);
    }

    @Override
    public int readBytes(GatheringByteChannel gatheringbytechannel, int i) throws IOException {
        return byteBuf.readBytes(gatheringbytechannel, i);
    }

    @Override
    public ByteBuf skipBytes(int i) {
        return byteBuf.skipBytes(i);
    }

    @Override
    public ByteBuf writeBoolean(boolean flag) {
        return byteBuf.writeBoolean(flag);
    }

    @Override
    public ByteBuf writeByte(int i) {
        return byteBuf.writeByte(i);
    }

    @Override
    public ByteBuf writeShort(int i) {
        return byteBuf.writeShort(i);
    }

    @Override
    public ByteBuf writeMedium(int i) {
        return byteBuf.writeMedium(i);
    }

    @Override
    public ByteBuf writeInt(int i) {
        return byteBuf.writeInt(i);
    }

    @Override
    public ByteBuf writeLong(long i) {
        return byteBuf.writeLong(i);
    }

    @Override
    public ByteBuf writeChar(int i) {
        return byteBuf.writeChar(i);
    }

    @Override
    public ByteBuf writeFloat(float f) {
        return byteBuf.writeFloat(f);
    }

    @Override
    public ByteBuf writeDouble(double d0) {
        return byteBuf.writeDouble(d0);
    }

    @Override
    public ByteBuf writeBytes(ByteBuf bytebuf) {
        return byteBuf.writeBytes(bytebuf);
    }

    @Override
    public ByteBuf writeBytes(ByteBuf bytebuf, int i) {
        return byteBuf.writeBytes(bytebuf, i);
    }

    @Override
    public ByteBuf writeBytes(ByteBuf bytebuf, int i, int j) {
        return byteBuf.writeBytes(bytebuf, i, j);
    }

    @Override
    public ByteBuf writeBytes(byte abyte[]) {
        return byteBuf.writeBytes(abyte);
    }

    @Override
    public ByteBuf writeBytes(byte abyte[], int i, int j) {
        return byteBuf.writeBytes(abyte, i, j);
    }

    @Override
    public ByteBuf writeBytes(ByteBuffer bytebuffer) {
        return byteBuf.writeBytes(bytebuffer);
    }

    @Override
    public int writeBytes(InputStream inputstream, int i) throws IOException {
        return byteBuf.writeBytes(inputstream, i);
    }

    @Override
    public int writeBytes(ScatteringByteChannel scatteringbytechannel, int i) throws IOException {
        return byteBuf.writeBytes(scatteringbytechannel, i);
    }

    @Override
    public ByteBuf writeZero(int i) {
        return byteBuf.writeZero(i);
    }

    @Override
    public int indexOf(int i, int j, byte b0) {
        return byteBuf.indexOf(i, j, b0);
    }

    @Override
    public int bytesBefore(byte b0) {
        return byteBuf.bytesBefore(b0);
    }

    @Override
    public int bytesBefore(int i, byte b0) {
        return byteBuf.bytesBefore(i, b0);
    }

    @Override
    public int bytesBefore(int i, int j, byte b0) {
        return byteBuf.bytesBefore(i, j, b0);
    }

    @Override
    public ByteBuf copy() {
        return byteBuf.copy();
    }

    @Override
    public ByteBuf copy(int i, int j) {
        return byteBuf.copy(i, j);
    }

    @Override
    public ByteBuf slice() {
        return byteBuf.slice();
    }

    @Override
    public ByteBuf slice(int i, int j) {
        return byteBuf.slice(i, j);
    }

    @Override
    public ByteBuf duplicate() {
        return byteBuf.duplicate();
    }

    @Override
    public int nioBufferCount() {
        return byteBuf.nioBufferCount();
    }

    @Override
    public ByteBuffer nioBuffer() {
        return byteBuf.nioBuffer();
    }

    @Override
    public ByteBuffer nioBuffer(int i, int j) {
        return byteBuf.nioBuffer(i, j);
    }

    @Override
    public ByteBuffer internalNioBuffer(int i, int j) {
        return byteBuf.internalNioBuffer(i, j);
    }

    @Override
    public ByteBuffer[] nioBuffers() {
        return byteBuf.nioBuffers();
    }

    @Override
    public ByteBuffer[] nioBuffers(int i, int j) {
        return byteBuf.nioBuffers(i, j);
    }

    @Override
    public boolean hasArray() {
        return byteBuf.hasArray();
    }

    @Override
    public byte[] array() {
        return byteBuf.array();
    }

    @Override
    public int arrayOffset() {
        return byteBuf.arrayOffset();
    }

    @Override
    public boolean hasMemoryAddress() {
        return byteBuf.hasMemoryAddress();
    }

    @Override
    public long memoryAddress() {
        return byteBuf.memoryAddress();
    }

    @Override
    public String toString(Charset charset) {
        return byteBuf.toString(charset);
    }

    @Override
    public String toString(int i, int j, Charset charset) {
        return byteBuf.toString(i, j, charset);
    }

    @Override
    public int hashCode() {
        return byteBuf.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        return byteBuf.equals(object);
    }

    @Override
    public int compareTo(ByteBuf bytebuf) {
        return byteBuf.compareTo(bytebuf);
    }

    @Override
    public String toString() {
        return byteBuf.toString();
    }

    @Override
    public ByteBuf retain(int i) {
        return byteBuf.retain(i);
    }

    @Override
    public ByteBuf retain() {
        return byteBuf.retain();
    }

    @Override
    public int refCnt() {
        return byteBuf.refCnt();
    }

    @Override
    public boolean release() {
        return byteBuf.release();
    }

    @Override
    public boolean release(int i) {
        return byteBuf.release(i);
    }

    @Override
    public ByteBuf asReadOnly() {
        return byteBuf.asReadOnly();
    }

    @Override
    public int forEachByte(ByteProcessor byteProcessor) {
        return byteBuf.forEachByte(byteProcessor);
    }

    @Override
    public int forEachByte(int i, int j, ByteProcessor byteProcessor) {
        return byteBuf.forEachByte(i, j, byteProcessor);
    }

    @Override
    public int forEachByteDesc(ByteProcessor byteProcessor) {
        return byteBuf.forEachByteDesc(byteProcessor);
    }

    @Override
    public int forEachByteDesc(int i, int j, ByteProcessor byteProcessor) {
        return byteBuf.forEachByteDesc(i, j, byteProcessor);
    }

    @Override
    public int getBytes(int i, FileChannel fileChannel, long l, int j) throws IOException {
        return byteBuf.getBytes(i, fileChannel, l, j);
    }

    @Override
    public CharSequence getCharSequence(int i, int j, Charset charset) {
        return byteBuf.getCharSequence(i, j, charset);
    }

    @Override
    public int getIntLE(int i) {
        return byteBuf.getIntLE(i);
    }

    @Override
    public long getLongLE(int i) {
        return byteBuf.getLongLE(i);
    }

    @Override
    public int getMediumLE(int i) {
        return byteBuf.getMediumLE(i);
    }

    @Override
    public short getShortLE(int i) {
        return byteBuf.getShortLE(i);
    }

    @Override
    public long getUnsignedIntLE(int i) {
        return byteBuf.getUnsignedIntLE(i);
    }

    @Override
    public int getUnsignedMediumLE(int i) {
        return byteBuf.getUnsignedMediumLE(i);
    }

    @Override
    public int getUnsignedShortLE(int i) {
        return byteBuf.getUnsignedShortLE(i);
    }

    @Override
    public boolean isReadOnly() {
        return byteBuf.isReadOnly();
    }

    @Override
    public int readBytes(FileChannel fileChannel, long l, int i) throws IOException {
        return byteBuf.readBytes(fileChannel, l, i);
    }

    @Override
    public CharSequence readCharSequence(int i, Charset charset) {
        return byteBuf.readCharSequence(i, charset);
    }

    @Override
    public int readIntLE() {
        return byteBuf.readIntLE();
    }

    @Override
    public long readLongLE() {
        return byteBuf.readLongLE();
    }

    @Override
    public int readMediumLE() {
        return byteBuf.readMediumLE();
    }

    @Override
    public ByteBuf readRetainedSlice(int i) {
        return byteBuf.readRetainedSlice(i);
    }

    @Override
    public short readShortLE() {
        return byteBuf.readShortLE();
    }

    @Override
    public long readUnsignedIntLE() {
        return byteBuf.readUnsignedIntLE();
    }

    @Override
    public int readUnsignedMediumLE() {
        return byteBuf.readUnsignedMediumLE();
    }

    @Override
    public int readUnsignedShortLE() {
        return byteBuf.readUnsignedShortLE();
    }

    @Override
    public ByteBuf retainedDuplicate() {
        return byteBuf.retainedDuplicate();
    }

    @Override
    public ByteBuf retainedSlice() {
        return byteBuf.retainedSlice();
    }

    @Override
    public ByteBuf retainedSlice(int i, int j) {
        return byteBuf.retainedSlice(i, j);
    }

    @Override
    public int setBytes(int i, FileChannel fileChannel, long l, int j) throws IOException {
        return byteBuf.setBytes(i, fileChannel, l, j);
    }

    @Override
    public int setCharSequence(int i, CharSequence charSequence, Charset charset) {
        return byteBuf.setCharSequence(i, charSequence, charset);
    }

    @Override
    public ByteBuf setIntLE(int i, int j) {
        return byteBuf.setIntLE(i, j);
    }

    @Override
    public ByteBuf setLongLE(int i, long l) {
        return byteBuf.setLongLE(i, l);
    }

    @Override
    public ByteBuf setMediumLE(int i, int j) {
        return byteBuf.setMediumLE(i, j);
    }

    @Override
    public ByteBuf setShortLE(int i, int j) {
        return byteBuf.setShortLE(i, j);
    }

    @Override
    public ByteBuf touch() {
        return byteBuf.touch();
    }

    @Override
    public ByteBuf touch(Object object) {
        return byteBuf.touch(object);
    }

    @Override
    public int writeBytes(FileChannel fileChannel, long l, int i) throws IOException {
        return byteBuf.writeBytes(fileChannel, l, i);
    }

    @Override
    public int writeCharSequence(CharSequence charSequence, Charset charset) {
        return byteBuf.writeCharSequence(charSequence, charset);
    }

    @Override
    public ByteBuf writeIntLE(int i) {
        return byteBuf.writeIntLE(i);
    }

    @Override
    public ByteBuf writeLongLE(long l) {
        return byteBuf.writeLongLE(l);
    }

    @Override
    public ByteBuf writeMediumLE(int i) {
        return byteBuf.writeMediumLE(i);
    }

    @Override
    public ByteBuf writeShortLE(int i) {
        return byteBuf.writeShortLE(i);
    }
}
