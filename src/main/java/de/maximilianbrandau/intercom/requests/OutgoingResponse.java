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

package de.maximilianbrandau.intercom.requests;

import de.maximilianbrandau.intercom.codec.IntercomByteBuf;
import de.maximilianbrandau.intercom.codec.IntercomCodec;
import de.maximilianbrandau.intercom.codec.packets.ResponsePacket;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class OutgoingResponse<T> {

    private final IntercomCodec<T> codec;
    private final ChannelHandlerContext ctx;
    private final Request<T> request;
    private short status = 200;
    private T data;

    public OutgoingResponse(IntercomCodec<T> codec, ChannelHandlerContext ctx, Request<T> request) {
        this.codec = codec;
        this.ctx = ctx;
        this.request = request;
    }

    public Request<T> getRequest() {
        return request;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean isOk() {
        return status == 200;
    }

    public void setOk(boolean ok) {
        status = (short) (ok ? 200 : 500);
    }

    public void end() {
        IntercomByteBuf dataBuffer = new IntercomByteBuf(Unpooled.buffer());
        this.codec.encode(getData(), dataBuffer);
        ctx.writeAndFlush(new ResponsePacket(getRequest().getRequestId(), status, dataBuffer));
    }

}
