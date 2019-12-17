package cn.katoumegumi.java.http.server.handle;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class WebSocketOutHandler extends ChannelOutboundHandlerAdapter {
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        String str = (String) msg;
        TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame(str);
        ctx.write(textWebSocketFrame,promise).addListener(ChannelFutureListener.CLOSE);
    }
}
