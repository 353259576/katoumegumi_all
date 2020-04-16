package cn.katoumegumi.java.http.server.initializer;

import cn.katoumegumi.java.http.server.handle.Http2RequestHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerUpgradeHandler;
import io.netty.handler.codec.http2.*;
import io.netty.handler.ssl.*;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.util.AsciiString;
import io.netty.util.ReferenceCountUtil;

import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;

public class Http2Initailzer extends ChannelInitializer<SocketChannel> {


    public static SslContext sslContext;

    static {
        try {
            SslProvider sslProvider = SslProvider.JDK;
            SelfSignedCertificate selfSignedCertificate = new SelfSignedCertificate();
            SslContextBuilder sslContextBuilder = SslContextBuilder.forServer(selfSignedCertificate.certificate(), selfSignedCertificate.privateKey());
            sslContextBuilder.sslProvider(sslProvider);
            sslContextBuilder.ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE);
            ApplicationProtocolConfig applicationProtocolConfig = new ApplicationProtocolConfig(
                    ApplicationProtocolConfig.Protocol.ALPN,
                    ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                    ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                    ApplicationProtocolNames.HTTP_2,
                    ApplicationProtocolNames.HTTP_1_1
            );
            sslContextBuilder.applicationProtocolConfig(applicationProtocolConfig);
            sslContext = sslContextBuilder.build();
            sslContext = null;
        } catch (SSLException | CertificateException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
       /* Http2HeadersEncoder http2HeadersEncoder = new DefaultHttp2HeadersEncoder();
        Http2HeadersDecoder http2HeadersDecoder = new DefaultHttp2HeadersDecoder();
        Http2ConnectionHandler http2ConnectionHandler = new Http2ConnectionHandler();
        new Http2ServerUpgradeCodec();*/

        ChannelPipeline channelPipeline = socketChannel.pipeline();
        if (sslContext != null) {
            channelPipeline.addLast(sslContext.newHandler(socketChannel.alloc()));
            channelPipeline.addLast(new ApplicationProtocolNegotiationHandler(ApplicationProtocolNames.HTTP_1_1) {
                @Override
                protected void configurePipeline(ChannelHandlerContext ctx, String protocol) throws Exception {
                    if (ApplicationProtocolNames.HTTP_2.equals(protocol)) {
                        System.out.println("支持http2协议");
                        //Http2Connection http2Connection = new DefaultHttp2Connection(true);
                        //Http2FrameWriter http2FrameWriter = new DefaultHttp2FrameWriter();
                        //Http2FrameReader http2FrameReader = new DefaultHttp2FrameReader();
                        //Http2ConnectionEncoder encoder = new DefaultHttp2ConnectionEncoder(http2Connection,http2FrameWriter);
                        //Http2ConnectionDecoder decoder = new DefaultHttp2ConnectionDecoder(http2Connection,encoder,http2FrameReader);
                        Http2RequestHandler http2RequestHandler = new Http2RequestHandler();
                        Http2ConnectionHandlerBuilder http2ConnectionHandlerBuilder = new Http2ConnectionHandlerBuilder();
                        http2ConnectionHandlerBuilder.server(true);
                        //http2ConnectionHandlerBuilder.connection(http2Connection);
                        //http2ConnectionHandlerBuilder.codec(decoder,encoder);
                        http2ConnectionHandlerBuilder.frameListener(http2RequestHandler);
                        Http2ConnectionHandler http2ConnectionHandler = http2ConnectionHandlerBuilder.build();
                        http2RequestHandler.setEncoder(http2ConnectionHandler.encoder());
                        http2RequestHandler.setDecoder(http2ConnectionHandler.decoder());
                        ctx.pipeline().addLast(http2ConnectionHandler);
                    } else {
                        System.out.println(protocol);
                        ctx.pipeline().addLast(new HttpServerCodec());
                        ctx.pipeline().addLast(new HttpObjectAggregator(1024 * 1024));
                        //ctx.pipeline().addLast(new HelloWorldHttp1Handler("this is http1.1 protocol"));

                    }
                }
            });
        } else {
            HttpServerCodec httpServerCodec = new HttpServerCodec();
            channelPipeline.addLast(httpServerCodec);
            channelPipeline.addLast(new HttpServerUpgradeHandler(httpServerCodec, new HttpServerUpgradeHandler.UpgradeCodecFactory() {
                @Override
                public HttpServerUpgradeHandler.UpgradeCodec newUpgradeCodec(CharSequence protocol) {
                    System.out.println("当前协议为:" + protocol);
                    if (AsciiString.contentEquals(Http2CodecUtil.HTTP_UPGRADE_PROTOCOL_NAME, protocol)) {
                        Http2RequestHandler http2RequestHandler = new Http2RequestHandler();
                        Http2ConnectionHandlerBuilder http2ConnectionHandlerBuilder = new Http2ConnectionHandlerBuilder();
                        http2ConnectionHandlerBuilder.server(true);
                        //http2ConnectionHandlerBuilder.connection(http2Connection);
                        //http2ConnectionHandlerBuilder.codec(decoder,encoder);
                        http2ConnectionHandlerBuilder.frameListener(http2RequestHandler);
                        Http2ConnectionHandler http2ConnectionHandler = http2ConnectionHandlerBuilder.build();
                        http2RequestHandler.setEncoder(http2ConnectionHandler.encoder());
                        http2RequestHandler.setDecoder(http2ConnectionHandler.decoder());
                        return new Http2ServerUpgradeCodec(http2ConnectionHandler);
                    } else {
                        return null;
                    }
                }
            }));

            channelPipeline.addLast(new SimpleChannelInboundHandler() {
                @Override
                protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
                    // If this handler is hit then no upgrade has been attempted and the client is just talking HTTP.
                    HttpMessage httpMessage = (HttpMessage) msg;
                    System.err.println("Directly talking: " + httpMessage.protocolVersion() + " (no upgrade was attempted)");
                    ChannelPipeline pipeline = ctx.pipeline();
                    //pipeline.addAfter(ctx.name(), null, new HelloWorldHttp1Handler("Direct. No Upgrade Attempted."));
                    pipeline.replace(this, null, new HttpObjectAggregator(1024 * 1024));
                    ctx.fireChannelRead(ReferenceCountUtil.retain(msg));
                }
            });
        }

    }
}
