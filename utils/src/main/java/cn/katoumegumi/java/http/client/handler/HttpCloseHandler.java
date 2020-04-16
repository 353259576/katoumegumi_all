package cn.katoumegumi.java.http.client.handler;

import cn.katoumegumi.java.http.client.model.HttpRequestBody;
import cn.katoumegumi.java.http.client.processor.WsChannelHttpRequestBodyBind;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;


public class HttpCloseHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(HttpCloseHandler.class);
    public static AtomicInteger atomicInteger = new AtomicInteger(0);
    private HttpRequestBody httpRequestBody;
    private String id;

    public HttpCloseHandler(String id, HttpRequestBody httpRequestBody) {
        this.httpRequestBody = httpRequestBody;
        this.id = id;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        WsChannelHttpRequestBodyBind.notCloseChannel.remove(ctx.channel().id().asLongText());
        atomicInteger.getAndAdd(1);
        log.info("{}触发通过关闭{}", id, httpRequestBody.getUrl());
        WsChannelHttpRequestBodyBind.removeChannelAndHttpRequestBodyBind(id);
        WsChannelHttpRequestBodyBind.removeResumableChannel(httpRequestBody, ctx.channel());
        WsChannelHttpRequestBodyBind.reconnectLink(httpRequestBody, id, ctx.channel());
        ctx.fireChannelUnregistered();
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        super.channelInactive(ctx);
    }
}
