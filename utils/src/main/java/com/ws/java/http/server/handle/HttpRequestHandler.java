package com.ws.java.http.server.handle;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.Map;

@Slf4j
public class HttpRequestHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object object) throws Exception {
        FullHttpRequest fullHttpRequest = (FullHttpRequest)object;
        String uri = fullHttpRequest.uri();
        String mehodName = fullHttpRequest.method().name();
        log.info("请求连接为：{}",uri);
        log.info("请求方式为：{}",mehodName);
        HttpHeaders httpHeaders = fullHttpRequest.headers();
        Iterator<Map.Entry<String, String>> iterator = httpHeaders.iteratorAsString();
        while (iterator.hasNext()){
            Map.Entry<String, String> entry = iterator.next();
            log.info("{}：{}",entry.getKey(),entry.getValue());
        }
        ByteBuf byteBuf = fullHttpRequest.content();
        int start = byteBuf.readerIndex();
        int end = byteBuf.readableBytes();
        byte bytes[] = new byte[end-start];
        byteBuf.readBytes(bytes,start,end);
        log.info("\r\n"+new String(bytes));

        channelHandlerContext.write(fullHttpRequest);
        channelHandlerContext.flush();
    }
}
