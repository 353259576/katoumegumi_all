package cn.katoumegumi.java.http.client.handler;

import cn.katoumegumi.java.http.client.processor.WsChannelHttpRequestBodyBind;
import cn.katoumegumi.java.http.client.model.HttpRequestBody;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class HttpCloseHandler extends ChannelInboundHandlerAdapter {

    private HttpRequestBody httpRequestBody;
    private String id;

    public static AtomicInteger atomicInteger = new AtomicInteger(0);

    public HttpCloseHandler(String id, HttpRequestBody httpRequestBody){
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
        log.info("{}触发通过关闭{}",id,httpRequestBody.getUrl());
        WsChannelHttpRequestBodyBind.removeChannelAndHttpRequestBodyBind(id);
        WsChannelHttpRequestBodyBind.removeResumableChannel(httpRequestBody,ctx.channel());
        WsChannelHttpRequestBodyBind.reconnectLink(httpRequestBody,id,ctx.channel());
        ctx.fireChannelUnregistered();
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        super.channelInactive(ctx);
    }
}
