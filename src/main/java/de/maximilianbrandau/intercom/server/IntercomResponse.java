package de.maximilianbrandau.intercom.server;

import de.maximilianbrandau.intercom.codec.IntercomByteBuf;
import de.maximilianbrandau.intercom.codec.packets.ResponsePacket;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class IntercomResponse<T> {

    private final IntercomServer<T> server;
    private final ChannelHandlerContext ctx;
    private final IntercomRequest<T> request;
    private short status = 200;
    private T data;

    public IntercomResponse(IntercomServer<T> server, ChannelHandlerContext ctx, IntercomRequest<T> request) {
        this.server = server;
        this.ctx = ctx;
        this.request = request;
    }

    public IntercomRequest<T> getRequest() {
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
        this.server.intercomCodec.encode(getData(), dataBuffer);
        ctx.writeAndFlush(new ResponsePacket(getRequest().getRequestId(), status, dataBuffer));
    }

}
