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

package de.maximilianbrandau.intercom.codec.packets;

import de.maximilianbrandau.intercom.codec.IntercomByteBuf;
import de.maximilianbrandau.intercom.codec.IntercomPacket;
import de.maximilianbrandau.intercom.codec.PacketType;

public class PingPacket extends IntercomPacket {

    private long startTime;
    private int lastPing;

    public PingPacket() {
    }

    public PingPacket(long startTime, int lastPing) {
        this.startTime = startTime;
        this.lastPing = lastPing;
    }

    public long getStartTime() {
        return startTime;
    }

    public int getLastPing() {
        return lastPing;
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.PING;
    }

    @Override
    public void encode(IntercomByteBuf byteBuffer) {
        byteBuffer.writeLong(startTime);
        byteBuffer.writeVarInt(lastPing);
    }

    @Override
    public void decode(IntercomByteBuf byteBuffer) {
        startTime = byteBuffer.readLong();
        lastPing = byteBuffer.readVarInt();
    }

}
