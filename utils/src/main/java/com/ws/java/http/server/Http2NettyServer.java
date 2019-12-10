package com.ws.java.http.server;

import com.ws.java.http.server.initializer.Http2Initailzer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.*;
import io.netty.handler.ssl.util.SelfSignedCertificate;

public class Http2NettyServer {


    public static void main(String[] args) throws Exception {

        /*SslContext sslContext = null;
        SslProvider sslProvider = SslProvider.JDK;
        SelfSignedCertificate selfSignedCertificate = new SelfSignedCertificate();
        SslContextBuilder sslContextBuilder = SslContextBuilder.forServer(selfSignedCertificate.certificate(),selfSignedCertificate.privateKey());
        sslContextBuilder.sslProvider(sslProvider);
        sslContextBuilder.ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE);
        ApplicationProtocolConfig applicationProtocolConfig = new ApplicationProtocolConfig(
                ApplicationProtocolConfig.Protocol.ALPN,
                ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                ApplicationProtocolNames.HTTP_1_1,
                ApplicationProtocolNames.HTTP_2
        );
        sslContextBuilder.applicationProtocolConfig(applicationProtocolConfig);
        sslContext = sslContextBuilder.build();*/

        EventLoopGroup workEventExecutors = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(workEventExecutors);
        serverBootstrap.channel(NioServerSocketChannel.class);
        serverBootstrap.handler(new LoggingHandler(LogLevel.INFO));
        serverBootstrap.option(ChannelOption.SO_BACKLOG,128);
        serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE,true);
        serverBootstrap.childHandler(new Http2Initailzer());
        try {
            Channel channel = serverBootstrap.bind(1996).sync().channel();
            channel.closeFuture().sync();
        }catch (InterruptedException e){
            e.printStackTrace();
        }


    }


}
