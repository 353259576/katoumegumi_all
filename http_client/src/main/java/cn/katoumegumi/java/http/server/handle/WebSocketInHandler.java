package cn.katoumegumi.java.http.server.handle;

import cn.katoumegumi.java.common.WsDateUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;


public class WebSocketInHandler extends SimpleChannelInboundHandler {

    private static final Logger log = LoggerFactory.getLogger(WebSocketInHandler.class);

    public static Map<String, Channel> map = new ConcurrentHashMap<>();

    private static volatile WebSocketServerHandshaker webSocketServerHandshaker;

    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    static {

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            log.info("键值对大小为：{}", map.size());
            long startTime = System.currentTimeMillis();
            Set<Map.Entry<String, Channel>> set = map.entrySet();
            Iterator<Map.Entry<String, Channel>> iterator = set.iterator();
            Flux.<Map.Entry<String, Channel>>create(entryFluxSink -> {
                while (iterator.hasNext()) {
                    entryFluxSink.next(iterator.next());
                }
                entryFluxSink.complete();
            }).subscribe(stringChannelEntry -> {
                Channel channel = stringChannelEntry.getValue();
                //ByteBuf byteBuf = Unpooled.copyInt(9);
                channel.writeAndFlush(new PingWebSocketFrame());
            });
            long endTime = System.currentTimeMillis();
            log.info("执行完成,共耗时{}毫秒", (endTime - startTime));
        }, 10, 10, TimeUnit.SECONDS);
/*        Flux.<Date>generate(fluxSink -> {
            fluxSink.next(new Date());
        }).timeout(Duration.ofSeconds(1)).subscribe(date -> {
            Set<Map.Entry<String,Channel>> set = map.entrySet();
            Iterator<Map.Entry<String,Channel>> iterator = set.iterator();
            Flux.<Map.Entry<String,Channel>>create(entryFluxSink -> {
                while (iterator.hasNext()){
                    entryFluxSink.next(iterator.next());
                }
                entryFluxSink.complete();
            }).subscribe(stringChannelEntry -> {
                Channel channel = stringChannelEntry.getValue();
                channel.writeAndFlush(new TextWebSocketFrame(WsDateUtils.dateToString(date,WsDateUtils.CNLONGTIMESTRING)));
            });
        });*/
    }


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object textWebSocketFrame) throws Exception {
        /*ByteBuf byteBuf = textWebSocketFrame.content();
        int start = byteBuf.readerIndex();
        int end = byteBuf.readableBytes();
        byte bytes[] = new byte[end - start];
        byteBuf.readBytes(byteBuf,start,end);
        String str = new String(bytes);*/
      /*  String str = textWebSocketFrame.text();
        log.info("客户端的消息为{}",str);
        Channel channel = channelHandlerContext.channel();
        channel.writeAndFlush(new TextWebSocketFrame(str));
        channelHandlerContext.flush();*/
        //channelHandlerContext.flush();
        try {
            if (textWebSocketFrame instanceof FullHttpRequest) {
                handleRequest(channelHandlerContext, (FullHttpRequest) textWebSocketFrame);
            }

            if (textWebSocketFrame instanceof WebSocketFrame) {
                handleWebSocket(channelHandlerContext, (WebSocketFrame) textWebSocketFrame);
            }
        } finally {
            //ReferenceCountUtil.retain(textWebSocketFrame);
            //7ReferenceCountUtil.release(textWebSocketFrame);
        }

    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        log.info("用户{}上线", ctx.channel().id());
        map.put(ctx.channel().id().asLongText(), ctx.channel());
        log.info("通道数量为：{}", map.size());
        super.handlerAdded(ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        log.info("用户{}下线", ctx.channel().id());
        map.remove(ctx.channel().id().asLongText());
        log.info("通道数量为：{}", map.size());
        super.handlerRemoved(ctx);
    }


    private void handleRequest(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) {
        if (!fullHttpRequest.decoderResult().isSuccess() || (!"websocket".equals(fullHttpRequest.headers().get("Upgrade")))) {
            handleHttp(channelHandlerContext, fullHttpRequest);
            return;
        }


        WebSocketServerHandshakerFactory webSocketServerHandshakerFactory = new WebSocketServerHandshakerFactory("ws/localhost:1234/ws", null, false);
        webSocketServerHandshaker = webSocketServerHandshakerFactory.newHandshaker(fullHttpRequest);
        if (webSocketServerHandshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(channelHandlerContext.channel());
        } else {
            webSocketServerHandshaker.handshake(channelHandlerContext.channel(), fullHttpRequest);
        }
    }


    public void handleHttp(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) {

        ByteBuf byteBuf = fullHttpRequest.content();
        int start = byteBuf.readerIndex();
        int end = byteBuf.readableBytes();
        byte[] bytes = new byte[end - start];
        byteBuf.readBytes(bytes, start, end);
        log.info("HTTP传输的数据为：{}", new String(bytes));
        String str = "<html><head><title>Netty响应</title></head><body><div style=\"text-align:centre;\">当前时间为" + WsDateUtils.dateToString(new Date(), WsDateUtils.CNLONGTIMESTRING) + "</div></body></html>";
        try {
            byteBuf = Unpooled.copiedBuffer(str.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
            byteBuf = Unpooled.copiedBuffer(str.getBytes());
        }
        FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, byteBuf);
        channelHandlerContext.writeAndFlush(fullHttpResponse).addListener(ChannelFutureListener.CLOSE);
        //byteBuf.release();
    }


    private void handleWebSocket(ChannelHandlerContext channelHandlerContext, WebSocketFrame webSocketFrame) {
        if (webSocketFrame instanceof CloseWebSocketFrame) {
            webSocketServerHandshaker.close(channelHandlerContext.channel(), ((CloseWebSocketFrame) webSocketFrame).retain());
            return;
        }

        if (webSocketFrame instanceof PingWebSocketFrame) {
            channelHandlerContext.writeAndFlush(new PongWebSocketFrame(webSocketFrame.content().retain()));
            //channelHandlerContext.writeAndFlush(new PongWebSocketFrame());
            return;
        }
        if (webSocketFrame instanceof PongWebSocketFrame) {
            //channelHandlerContext.writeAndFlush(new PingWebSocketFrame(webSocketFrame.content().retain()));
            return;
        }
        String str = ((TextWebSocketFrame) webSocketFrame).text();
        //channelHandlerContext.writeAndFlush(new TextWebSocketFrame(str));
        channelHandlerContext.flush();
        executorService.submit(() -> sendMsg(channelHandlerContext.channel().id().asLongText(), str));
    }


    public void sendMsg(String channelId, String value) {
        Set<Map.Entry<String, Channel>> set = map.entrySet();
        Iterator<Map.Entry<String, Channel>> iterator = set.iterator();
        Flux.<Map.Entry<String, Channel>>create(channelFluxSink -> {
            while (iterator.hasNext()) {
                channelFluxSink.next(iterator.next());
            }
            channelFluxSink.complete();
        }).filter(entity -> {
            String id = entity.getKey();
            if (id.equals(channelId)) {
                return false;
            } else {
                return true;
            }
        }).subscribe(entity -> {
            String id = entity.getKey();
            Channel channel = entity.getValue();
            TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame(id + "：" + value);
            channel.writeAndFlush(textWebSocketFrame);
        });

    }


}
