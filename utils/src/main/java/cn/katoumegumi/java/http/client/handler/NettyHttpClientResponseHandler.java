package cn.katoumegumi.java.http.client.handler;

import cn.katoumegumi.java.http.client.processor.WsChannelHttpRequestBodyBind;
import cn.katoumegumi.java.http.client.model.ChannelTimeoutEntry;
import cn.katoumegumi.java.http.client.model.HttpRequestBody;
import cn.katoumegumi.java.http.client.model.HttpResponseBody;
import cn.katoumegumi.java.http.client.utils.WsNettyClientUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.ChannelInputShutdownEvent;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class NettyHttpClientResponseHandler extends ChannelInboundHandlerAdapter {
    private volatile HttpRequestBody httpRequestBody;
    private volatile String id;
    private volatile boolean sendHttp;

    public static AtomicInteger atomicInteger = new AtomicInteger(0);


    public NettyHttpClientResponseHandler(HttpRequestBody httpRequestBody, String id,boolean sendHttp){
        this.httpRequestBody = httpRequestBody;
        this.id = id;
        this.sendHttp = sendHttp;
    }


    public HttpRequestBody getHttpRequestBody() {
        return httpRequestBody;
    }

    public void setHttpRequestBody(HttpRequestBody httpRequestBody) {
        this.httpRequestBody = httpRequestBody;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            String channelId  = ctx.channel().id().asLongText();
            if(msg instanceof FullHttpResponse){
                FullHttpResponse fullHttpResponse = (FullHttpResponse)msg;
                HttpHeaders httpHeaders = fullHttpResponse.headers();
                HttpResponseBody httpResponseBody = HttpResponseBody.createHttpResponseBody();
                Iterator<Map.Entry<String,String>> iterator = httpHeaders.iteratorAsString();
                while (iterator.hasNext()){
                    Map.Entry<String,String> entry = iterator.next();
                    httpResponseBody.setHeaderProperty(entry.getKey(),entry.getValue());
                }
                ByteBuf byteBuf = fullHttpResponse.content();
                //ByteBuf byteBuf = (ByteBuf)msg;
                byte bytes[] = new byte[byteBuf.readableBytes()];
                byteBuf.readBytes(bytes);
                //System.out.println(new String(bytes));
                byteBuf.release();
                int code = fullHttpResponse.status().code();
                httpResponseBody.setCode(code);
                httpResponseBody.setUrl(httpRequestBody.getUrl());
                httpResponseBody.setHttpVersion(fullHttpResponse.protocolVersion().text());
                if(code == 200){
                    httpResponseBody.setReturnBytes(bytes);
                }else {
                    httpResponseBody.setErrorReturnBytes(bytes);
                }
                WsChannelHttpRequestBodyBind.idChannelTimeoutEntryMap.remove(id);
                ChannelTimeoutEntry channelTimeoutEntry = new ChannelTimeoutEntry();
                channelTimeoutEntry.setId(id);
                WsChannelHttpRequestBodyBind.delayQueue.remove(channelTimeoutEntry);
                WsChannelHttpRequestBodyBind.putHttpResponseBody(id,httpResponseBody);
                WsChannelHttpRequestBodyBind.removeChannelAndHttpRequestBodyBind(id);
                WsChannelHttpRequestBodyBind.semaphore.release();
                if(HttpHeaderValues.KEEP_ALIVE.toString().equals(fullHttpResponse.headers().get("Connection"))){
                    WsChannelHttpRequestBodyBind.addResumableChannel(httpRequestBody,ctx.channel());
                }else {
                    ctx.close();
                }
                atomicInteger.getAndAdd(1);

            }else if(msg instanceof HttpResponse){
                //long startTime = System.currentTimeMillis();
                HttpResponse httpResponse = (HttpResponse)msg;
                HttpHeaders httpHeaders = httpResponse.headers();
                HttpResponseBody httpResponseBody = HttpResponseBody.createHttpResponseBody();
                Iterator<Map.Entry<String,String>> iterator = httpHeaders.iteratorAsString();
                while (iterator.hasNext()){
                    Map.Entry<String,String> entry = iterator.next();
                    httpResponseBody.setHeaderProperty(entry.getKey(),entry.getValue());
                }
                int code = httpResponse.status().code();
                httpResponseBody.setCode(code);
                httpResponseBody.setUrl(httpRequestBody.getUrl());
                httpResponseBody.setHttpVersion(httpResponse.protocolVersion().text());
                httpResponseBody.setReturnBytes(new byte[0]);
                httpResponseBody.setErrorReturnBytes(new byte[0]);
                WsChannelHttpRequestBodyBind.httpResponseBodyMap.put(id,httpResponseBody);
                WsChannelHttpRequestBodyBind.stringByteBufMap.put(id,Unpooled.directBuffer());
                //long endTime = System.currentTimeMillis();
                //log.info("读取http头花费时间：{}",endTime - startTime);
            }else if(msg instanceof HttpContent) {
                //log.info("下载中");
                ByteBuf oldBytebuf = WsChannelHttpRequestBodyBind.stringByteBufMap.get(id);
                HttpContent httpContent = (HttpContent) msg;
                HttpResponseBody httpResponseBody = WsChannelHttpRequestBodyBind.httpResponseBodyMap.get(id);
                ByteBuf byteBuf = httpContent.content();
                oldBytebuf = oldBytebuf.writeBytes(byteBuf);
                byteBuf.release();
                if (msg instanceof LastHttpContent) {
                    byte[] bytes = new byte[oldBytebuf.readableBytes()];
                    oldBytebuf.readBytes(bytes);
                    if (httpResponseBody.getCode() == 200) {
                        httpResponseBody.setReturnBytes(bytes);
                    } else {
                        httpResponseBody.setErrorReturnBytes(bytes);
                    }
                    oldBytebuf.release();
                    WsChannelHttpRequestBodyBind.stringByteBufMap.remove(id);
                    WsChannelHttpRequestBodyBind.idChannelTimeoutEntryMap.remove(id);
                    ChannelTimeoutEntry channelTimeoutEntry = new ChannelTimeoutEntry();
                    channelTimeoutEntry.setId(id);
                    WsChannelHttpRequestBodyBind.delayQueue.remove(channelTimeoutEntry);
                    WsChannelHttpRequestBodyBind.countDownCountDownLatch(id);
                    WsChannelHttpRequestBodyBind.removeChannelAndHttpRequestBodyBind(id);
                    WsChannelHttpRequestBodyBind.semaphore.release();
                    List<String> list = httpResponseBody.getHeaderProperty("Connection");
                    if (list != null) {
                        if (list.contains(HttpHeaderValues.KEEP_ALIVE.toString())) {
                            WsChannelHttpRequestBodyBind.addResumableChannel(httpRequestBody,ctx.channel());
                        }else {
                            ctx.close();
                        }
                    }else {
                        ctx.close();
                    }
                    atomicInteger.getAndAdd(1);
                }
            }



            /*ctx.close().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    log.info("Channel：{}关闭",channelFuture.channel().id().asLongText());
                    channelFuture.channel().close();
                }
            });*/
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        try {
            if(sendHttp) {
                WsNettyClientUtils.sendHttpRequest(ctx, httpRequestBody, id);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        ctx.fireChannelActive();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        String channelId = ctx.channel().id().asLongText();
        //WsChannelHttpRequestBodyBind.countDownCountDownLatch(id);
        //WsChannelHttpRequestBodyBind.httpResponseBodyMap.remove(id);
        SocketAddress socketAddress = ctx.channel().remoteAddress();
        log.info(socketAddress.toString());
        /*Iterator<Map.Entry<String,ChannelHandler>> iterator = ctx.pipeline().iterator();
        while (iterator.hasNext()){
            Map.Entry<String,ChannelHandler> entry = iterator.next();
            log.info("名称：{}，CLASS：{}",entry.getKey(),entry.getValue().getClass());
        }*/

        ctx.close();
    }

/*    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        //atomicInteger.getAndAdd(1);
        super.channelUnregistered(ctx);
    }*/

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof ChannelInputShutdownEvent){
            log.info("服务器主动断开");
            ctx.close();
        }
        /*if(evt instanceof IdleStateEvent){
            if(((IdleStateEvent)evt).state().equals(IdleState.ALL_IDLE)){
                String channelId = ctx.channel().id().asLongText();
                log.info("超时，准备关闭{}",channelId);
                //WsChannelHttpRequestBodyBind.countDownCountDownLatch(channelId);
                ctx.close();
                log.info("关闭channel完成");
                WsNettyClient.clientStart(httpRequestBody,id);
            }

        }else {
            super.userEventTriggered(ctx,evt);
        }*/
        //super.userEventTriggered(ctx,evt);
    }
}
