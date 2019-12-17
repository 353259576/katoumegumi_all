package cn.katoumegumi.java.http.server.handle;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;

@Slf4j
public class OutServerHandle extends ChannelOutboundHandlerAdapter {
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        ByteBuf byteBuf = (ByteBuf)msg;
        log.info("进入了write了");
        String str =  "HTTP/1.1 200\n" +
                "Date: Wed, 21 Nov 2018 07:39:48 GMT\n" +
                "Content-Type: text/html; charset=UTF-8\n" +
                "Connection: close\n" +
                "Cache-Control: no-cache, must-revalidate\n" +
                "Timestamp: 15:39:48.130\n" +
                "\n" +
                "你好世界";
        byteBuf.writeBytes(str.getBytes());
        //Thread.sleep(10000);
        ChannelFuture channelFuture = ctx.write(byteBuf,promise);
        channelFuture.addListener(ChannelFutureListener.CLOSE);
       /* channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                log.info("会话结束");
                ReferenceCountUtil.release(msg);
                ctx.close();
            }
        });
        super.write(ctx, msg, promise);*/
    }

    @Override
    public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        super.bind(ctx, localAddress, promise);
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        super.connect(ctx, remoteAddress, localAddress, promise);
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        super.disconnect(ctx, promise);
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        super.close(ctx, promise);
    }

    @Override
    public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        super.deregister(ctx, promise);
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        super.flush(ctx);
    }
}
