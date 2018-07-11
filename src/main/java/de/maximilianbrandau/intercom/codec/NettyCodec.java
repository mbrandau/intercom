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

import de.maximilianbrandau.intercom.codec.packets.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

public class NettyCodec extends ByteToMessageCodec<IntercomPacket> {

    private static IntercomPacket decode(ByteBuf byteBuffer) {
        PacketType packetType = PacketType.getById(byteBuffer.readByte());
        int length = byteBuffer.readInt();
        ByteBuf data = byteBuffer.readSlice(length);
        IntercomPacket packet = null;
        assert packetType != null;
        switch (packetType) {
            case AUTH:
                packet = new AuthPacket();
                break;
            case AUTH_RESPONSE:
                packet = new AuthResponsePacket();
                break;
            case PING:
                packet = new PingPacket();
                break;
            case REQUEST:
                packet = new RequestPacket();
                break;
            case RESPONSE:
                packet = new ResponsePacket();
                break;
            case PUSH:
                packet = new PushPacket();
                break;
        }

        packet.decode(new IntercomByteBuf(data));

        return packet;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, IntercomPacket intercomPacket, ByteBuf byteBuf) {
        ByteBuf dataBuffer = Unpooled.buffer();
        intercomPacket.encode(new IntercomByteBuf(dataBuffer));

        byteBuf.writeByte(intercomPacket.getPacketType().getId());
        byteBuf.writeInt(dataBuffer.writerIndex());
        byteBuf.writeBytes(dataBuffer);
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) {
        list.add(decode(byteBuf));
    }
}
