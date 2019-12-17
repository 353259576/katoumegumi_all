package cn.katoumegumi.java.http.server;

import cn.katoumegumi.java.http.server.initializer.WebSocketInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WsNettyServer {

    public static void main(String[] args) {
        WsNettyServer wsNettyServer = new WsNettyServer();
        wsNettyServer.nettyStart(1234);
        log.info("log启动");
    }


    public void nettyStart(Integer port){
        log.info("netty服务启动开始");
        EventLoopGroup bossEventExecutors = new NioEventLoopGroup(1);
        EventLoopGroup workEventExecutors = new NioEventLoopGroup(3);
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossEventExecutors,workEventExecutors);
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.handler(new LoggingHandler(LogLevel.WARN));
            serverBootstrap.childHandler(new WebSocketInitializer());
            serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024);
            serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE,true);
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            channelFuture.channel().closeFuture().sync();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            log.info("netty结束");
            workEventExecutors.shutdownGracefully();
            bossEventExecutors.shutdownGracefully();
        }
    }
}
