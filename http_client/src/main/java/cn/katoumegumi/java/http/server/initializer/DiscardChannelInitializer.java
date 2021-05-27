package cn.katoumegumi.java.http.server.initializer;

import cn.katoumegumi.java.http.server.handle.HttpRequestHandler;
import cn.katoumegumi.java.http.server.handle.HttpResponseHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

public class DiscardChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        //socketChannel.pipeline().addLast(new OutServerHandle());
        //socketChannel.pipeline().addLast(new InServerHandler());

        //socketChannel.pipeline().addLast(new HttpServerCodec());

        socketChannel.pipeline().addLast(new HttpRequestDecoder());
        socketChannel.pipeline().addLast(new HttpResponseEncoder());
        socketChannel.pipeline().addLast("httpAggregator", new HttpObjectAggregator(512 * 1024 * 1024));
        socketChannel.pipeline().addLast(new HttpResponseHandler());
        socketChannel.pipeline().addLast(new HttpRequestHandler());

    }
}
