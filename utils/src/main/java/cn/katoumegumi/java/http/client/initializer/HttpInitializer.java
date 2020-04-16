package cn.katoumegumi.java.http.client.initializer;

import cn.katoumegumi.java.http.client.handler.Http2SettingsHandler;
import cn.katoumegumi.java.http.client.handler.HttpCloseHandler;
import cn.katoumegumi.java.http.client.handler.HttpResponseHandler;
import cn.katoumegumi.java.http.client.handler.NettyHttpClientResponseHandler;
import cn.katoumegumi.java.http.client.model.HttpRequestBody;
import cn.katoumegumi.java.http.client.processor.WsChannelHttpRequestBodyBind;
import cn.katoumegumi.java.http.client.utils.WsNettyClientUtils;
import cn.katoumegumi.java.http.client.utils.WsSSLContext;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http2.*;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.handler.logging.LogLevel.INFO;

public class HttpInitializer extends ChannelInitializer<SocketChannel> {

    private static final Logger log = LoggerFactory.getLogger(HttpInitializer.class);
    private static final Http2FrameLogger LOGGER = new Http2FrameLogger(INFO, HttpInitializer.class);
    public HttpRequestBody httpRequestBody;
    String id;


    public HttpInitializer(HttpRequestBody httpRequestBody, String id) {
        this.httpRequestBody = httpRequestBody;
        this.id = id;
    }


    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        WsChannelHttpRequestBodyBind.notCloseChannel.put(socketChannel.pipeline().channel().id().asLongText(), socketChannel.pipeline().channel());
        long startTime = System.currentTimeMillis();
        socketChannel.pipeline().addLast(new HttpCloseHandler(id, httpRequestBody));
        if (httpRequestBody.isHttps()) {
            if (httpRequestBody.getPkcsPath() == null) {
                socketChannel.pipeline().addLast(WsSSLContext.createNettySslContext(false, null).newHandler(socketChannel.alloc()));
                socketChannel.pipeline().addLast(new ApplicationProtocolNegotiationHandler("") {
                    @Override
                    protected void configurePipeline(ChannelHandlerContext ctx, String protocol) {
                        if (ApplicationProtocolNames.HTTP_2.equals(protocol)) {
                            log.info("支持http2");
                        /*ChannelPipeline p = ctx.pipeline();
                        p.addLast(connectionHandler);
                        configureEndOfPipeline(p);
                        return;*/

                            final Http2Connection connection = new DefaultHttp2Connection(false);
                            HttpToHttp2ConnectionHandler connectionHandler = new HttpToHttp2ConnectionHandlerBuilder()
                                    .frameListener(new DelegatingDecompressorFrameListener(
                                            connection,
                                            new InboundHttp2ToHttpAdapterBuilder(connection)
                                                    .maxContentLength(Integer.MAX_VALUE)
                                                    .propagateSettings(true)
                                                    .build()))
                                    .frameLogger(LOGGER)
                                    .connection(connection)
                                    .build();
                            HttpResponseHandler responseHandler = new HttpResponseHandler();
                            Http2SettingsHandler settingsHandler = new Http2SettingsHandler(socketChannel.newPromise());

                            socketChannel.pipeline().addLast(connectionHandler);
                            socketChannel.pipeline().addLast(settingsHandler, responseHandler);

                            new Thread(() -> {
                                try {
                                    settingsHandler.awaitSettings(5, TimeUnit.SECONDS);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                int streamId = 3;


                                for (int i = 0; i < 1; i++) {
                                    FullHttpRequest request = new DefaultFullHttpRequest(HTTP_1_1, GET, httpRequestBody.getUri().getPath(), Unpooled.EMPTY_BUFFER);
                                    request.headers().add(HttpHeaderNames.HOST, httpRequestBody.getUri().getHost());
                                    request.headers().add(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text(), HttpScheme.HTTPS);
                                    request.headers().add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
                                    request.headers().add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.DEFLATE);
                                    responseHandler.put(streamId, ctx.channel().write(request), ctx.channel().newPromise());
                                    streamId += 2;


                                    ctx.channel().flush();
                                    responseHandler.awaitResponses(5, TimeUnit.SECONDS);
                                }

                            }).start();


                        } else {
                            //包含编码器和解码器
                            httpInitializer(socketChannel, httpRequestBody, id, false);
                            //httpInitializer(socketChannel, httpRequestBody, id);


                        }
                    }
                });
            } else {
                socketChannel.pipeline().addLast(WsSSLContext.createNettySslContext(false, WsSSLContext.keyManagerFactory(httpRequestBody.getPkcsPath(), httpRequestBody.getPkcsPassword(), "PKCS12")).newHandler(socketChannel.alloc()));
                httpInitializer(socketChannel, httpRequestBody, id, true);
            }

        } else {
            httpInitializer(socketChannel, httpRequestBody, id, true);
        }

        long endTime = System.currentTimeMillis();
        //log.info("解析https花费时间为：{}",endTime-startTime);

    }


    public void httpInitializer(SocketChannel socketChannel, HttpRequestBody httpRequestBody, String id, boolean sendHttp) {
        socketChannel.pipeline().addLast(new HttpClientCodec());
        if (!httpRequestBody.isUseChunked()) {
            //聚合
            socketChannel.pipeline().addLast(new HttpObjectAggregator(1024 * 1024 * 64));
        }
        //解压
        //socketChannel.pipeline().addLast(new HttpContentDecompressor());
        socketChannel.pipeline().addLast(new ChunkedWriteHandler());
        //socketChannel.pipeline().addLast(new IdleStateHandler(0,0,60, TimeUnit.SECONDS));
        // 自定义处理handler
        socketChannel.pipeline().addLast("http-server", new NettyHttpClientResponseHandler(httpRequestBody, id, sendHttp));
        if (!sendHttp) {
            WsNettyClientUtils.sendHttpRequest(socketChannel.pipeline().channel(), httpRequestBody, id);
        }
    }
}
