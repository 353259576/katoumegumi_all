package cn.katoumegumi.java.http.client.utils;

import cn.katoumegumi.java.http.client.model.HttpRequestBody;
import cn.katoumegumi.java.http.client.model.WsRequestProperty;
import cn.katoumegumi.java.http.client.processor.WsChannelHttpRequestBodyBind;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.List;


public class WsNettyClientUtils {


    public static void sendHttpRequest(ChannelHandlerContext ctx, HttpRequestBody httpRequestBody, String id) {
        WsChannelHttpRequestBodyBind.putChannelDelay(ctx.channel(), httpRequestBody, id);
        /*FullHttpRequest fullHttpRequest = createFullHttpRequest(httpRequestBody);
        ctx.write(fullHttpRequest);
        ctx.write(LastHttpContent.EMPTY_LAST_CONTENT);
        ctx.flush();*/
        HttpContent httpContent = createHttpContent(httpRequestBody);
        HttpRequest httpRequest = createHttpRequest(httpRequestBody, httpContent);
        ctx.write(httpRequest);
        ctx.write(httpContent);
        ctx.write(LastHttpContent.EMPTY_LAST_CONTENT).addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                if (future.isSuccess()) {
                }
            }
        });
        ctx.flush();
    }

    public static void sendHttpRequest(Channel ctx, HttpRequestBody httpRequestBody, String id) {
        WsChannelHttpRequestBodyBind.putChannelDelay(ctx, httpRequestBody, id);
        /*FullHttpRequest fullHttpRequest = createFullHttpRequest(httpRequestBody);
        ctx.write(fullHttpRequest);
        ctx.write(LastHttpContent.EMPTY_LAST_CONTENT);
        ctx.flush();*/
        HttpContent httpContent = createHttpContent(httpRequestBody);
        HttpRequest httpRequest = createHttpRequest(httpRequestBody, httpContent);
        ctx.write(httpRequest);
        ctx.write(httpContent).addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                if (future.isSuccess()) {
                }
            }
        });
        ;
        //ctx.write(LastHttpContent.EMPTY_LAST_CONTENT);
        ctx.write(LastHttpContent.EMPTY_LAST_CONTENT).addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                if (future.isSuccess()) {
                }
            }
        });
        ctx.flush();
    }


    public static FullHttpRequest createFullHttpRequest(HttpRequestBody httpRequestBody) {
        ByteBuf byteBuf = WsNettyClientUtils.httpRequestToByteBuf(httpRequestBody);
        HttpMethod httpMethod = null;

        switch (httpRequestBody.getMethod()) {
            case "GET":
                httpMethod = HttpMethod.GET;
                break;
            case "POST":
                httpMethod = HttpMethod.POST;
                break;
            case "HEAD":
                httpMethod = HttpMethod.HEAD;
                break;
            default:
                httpMethod = HttpMethod.POST;
                break;
        }

        FullHttpRequest fullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, httpMethod, httpRequestBody.getUri().getPath(), byteBuf);
        return (FullHttpRequest) setHttpHeaders(fullHttpRequest, httpRequestBody, fullHttpRequest);
    }

    public static HttpRequest createHttpRequest(HttpRequestBody httpRequestBody, HttpContent httpContent) {
        HttpMethod httpMethod = null;

        switch (httpRequestBody.getMethod()) {
            case "GET":
                httpMethod = HttpMethod.GET;
                break;
            case "POST":
                httpMethod = HttpMethod.POST;
                break;
            case "HEAD":
                httpMethod = HttpMethod.HEAD;
                break;
            default:
                httpMethod = HttpMethod.POST;
                break;
        }

        HttpRequest httpRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, httpMethod, httpRequestBody.getUrl());
        return setHttpHeaders(httpRequest, httpRequestBody, httpContent);
    }

    public static HttpRequest setHttpHeaders(HttpRequest httpRequest, HttpRequestBody httpRequestBody, HttpContent httpContent) {

        httpRequest.headers().set(HttpHeaderNames.HOST, httpRequestBody.getUri().getHost());
        httpRequest.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        //fullHttpRequest.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
        httpRequest.headers().set(HttpHeaderNames.ACCEPT, "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2");
        httpRequest.headers().set(HttpHeaderNames.CONTENT_TYPE, httpRequestBody.getMediaType().getCoed());
        httpRequest.headers().set(HttpHeaderNames.CONTENT_LENGTH, httpContent.content().readableBytes());
        List<WsRequestProperty> list = httpRequestBody.getRequestProperty();
        list.stream().forEach(wsRequestProperty -> {
            httpRequest.headers().set(wsRequestProperty.getKey(), wsRequestProperty.getValue());
        });
        return httpRequest;
    }

    public static HttpContent createHttpContent(HttpRequestBody httpRequestBody) {
        ByteBuf byteBuf = WsNettyClientUtils.httpRequestToByteBuf(httpRequestBody);
        HttpContent httpContent = new DefaultHttpContent(byteBuf);
        return httpContent;
    }


    public static ByteBuf httpRequestToByteBuf(HttpRequestBody httpRequestBody) {
        byte bytes[] = null;
        try {
            bytes = httpRequestBody.getbyteHttpRequestBody();
        } catch (Exception e) {
            e.printStackTrace();
        }

        ByteBuf byteBuf = null;
        if (bytes != null) {
            byteBuf = Unpooled.copiedBuffer(httpRequestBody.getbyteHttpRequestBody());
        } else {
            byteBuf = Unpooled.buffer(0);
        }
        return byteBuf;
    }


}
