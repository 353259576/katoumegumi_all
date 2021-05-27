package cn.katoumegumi.java.http.server.handle;

import cn.katoumegumi.java.common.WsDateUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.util.Date;

public class HttpResponseHandler extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        //FullHttpRequest fullHttpRequest = (FullHttpRequest)msg;
        String val = "<html><head><title>Netty响应</title></head><body><div style=\"text-align:centre;\">当前时间为" + WsDateUtils.dateToString(new Date(), WsDateUtils.CNLONGTIMESTRING) + "</div></body></html>";
        ByteBuf byteBuf1 = Unpooled.copiedBuffer(val, CharsetUtil.UTF_8);
        FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, byteBuf1);
        fullHttpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
        ctx.write(fullHttpResponse, promise).addListener(ChannelFutureListener.CLOSE);
    }
}
