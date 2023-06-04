package cn.katoumegumi.java.http.server.handle;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class InServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(InServerHandler.class);


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("会话活动中");
        super.channelActive(ctx);
    }


    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        log.info("更改读写模式");
        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("读取进行");
        try {
            //super.channelRead(ctx, msg);
            ByteBuf byteBuf = (ByteBuf) msg;
            int start = byteBuf.readerIndex();
            int end = byteBuf.readableBytes();
            byte[] bytes = new byte[end - start];
            byteBuf.readBytes(bytes, start, end);
            //System.out.println(new String(bytes));
            log.info("\r\n" + new String(bytes));
            ChannelFuture channelFuture = ctx.write(msg);
            ctx.flush();
            /*channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    log.info("会话结束");
                    //ReferenceCountUtil.release(msg);
                    ctx.close();
                }
            });
            ctx.flush();*/
            //ctx.fireChannelRead(msg);

        } finally {
            ReferenceCountUtil.retain(msg);
            ReferenceCountUtil.retain(msg);
            ReferenceCountUtil.release(msg);
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
        ctx.close();
    }
}
