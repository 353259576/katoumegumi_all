package cn.katoumegumi.java.http.server.initializer;

import cn.katoumegumi.java.http.client.utils.WsSSLContext;
import cn.katoumegumi.java.http.server.handle.WebSocketInHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLEngine;


public class WebSocketInitializer extends ChannelInitializer<SocketChannel> {

    private static final Logger log = LoggerFactory.getLogger(WebSocketInHandler.class);

    private final static String URL = "D:/项目/环境/apache-tomcat-9.0.12 - 副本/conf/www.fishmaimai.com.jks";
    private final static String PASSWORD = "199645";

    public static volatile Integer i = 0;

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        i++;
        log.info(i.toString());
        ChannelPipeline channelPipeline = socketChannel.pipeline();
        //channelPipeline.addLast("httpRequestDecoder",new WebSocket07FrameEncoder(false));
        //channelPipeline.addLast("httpResponseEncoder",new WebSocket07FrameDecoder(false,false,7));
        SSLEngine sslEngine = WsSSLContext.getSSLContext(URL, PASSWORD, "jks").createSSLEngine();
        sslEngine.setUseClientMode(false);
        //SslContextBuilder sslContextBuilder = SslContextBuilder.forServer(WsSSLContext.keyManagerFactory(URL,PASSWORD,"jks"));
        //channelPipeline.addLast("SSL",new OptionalSslHandler(sslContextBuilder.build()));
        channelPipeline.addLast("SSL", new SslHandler(sslEngine));
        channelPipeline.addLast("httpServerCodec", new HttpServerCodec());
        channelPipeline.addLast("chunkedWriteHandler", new ChunkedWriteHandler());
        channelPipeline.addLast("httpObjectAggregator", new HttpObjectAggregator(8192));
        //channelPipeline.addLast("webSocketServerProtocolHandler",new WebSocketServerProtocolHandler("/ws"));
        //channelPipeline.addLast("webSocketOutHandler",new WebSocketOutHandler());
        channelPipeline.addLast("webSocketInHandler", new WebSocketInHandler());

    }
}
