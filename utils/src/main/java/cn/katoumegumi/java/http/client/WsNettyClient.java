package cn.katoumegumi.java.http.client;

import cn.katoumegumi.java.common.WsImageUtils;
import cn.katoumegumi.java.http.client.handler.NettyHttpClientResponseHandler;
import cn.katoumegumi.java.http.client.initializer.HttpInitializer;
import cn.katoumegumi.java.http.client.model.HttpRequestBody;
import cn.katoumegumi.java.http.client.model.HttpResponseBody;
import cn.katoumegumi.java.http.client.model.HttpResponseTask;
import cn.katoumegumi.java.http.client.processor.WsChannelHttpRequestBodyBind;
import cn.katoumegumi.java.http.client.utils.WsNettyClientUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


public class WsNettyClient {
    private static final Logger log = LoggerFactory.getLogger(WsNettyClient.class);

    public static volatile EventLoopGroup workEventExecutors = new NioEventLoopGroup();
    public static AtomicInteger atomicInteger = new AtomicInteger(0);
    public static AtomicInteger atomicInteger1 = new AtomicInteger(0);
    private static volatile ExecutorService executor = Executors.newFixedThreadPool(200);

    public static void main(String[] args) {
        System.setProperty("http.proxyHost", "localhost");
        System.setProperty("https.proxyHost", "127.0.0.1");
        System.setProperty("http.proxyPort", "8888");
        System.setProperty("https.proxyPort", "8888");

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long startTime = System.currentTimeMillis();
        String strs[] = new String[]{
                "https://www.baidu.com",
                "https://www.bilibili.com",
                "http://www.baidu.com",
                "http://www.bilibili.com",
                "https://www.sina.com.cn",
                "https://www.qq.com",
                "https://www.360.cn",
                "https://www.fishmaimai.com",
                "https://www.jd.com/?cu=true",
                "https://www.tmall.com/?ali_trackid=2:mm_26632322_6858406_70736499:1562211803_219_1783123944",
                "http://www.ifeng.com",
                "https://www.suning.com",
                "https://www.autohome.com.cn/beijing/",
                "https://www.taobao.com",
                "http://www.eastmoney.com",
                "https://hf.58.com/?spm=b-31580022738699-me-f-862.mingzhan",
                "https://www.ctrip.com",
                "https://www.douban.com",
                "https://www.booking.com/index.zh-cn.html?aid=1337411;label=bai408jc-1DCAEoggI46AdIM1gDaDGIAQGYASu4ARfIAQzYAQPoAQH4AQSIAgGoAgO4At3s9egFwAIB;sid=30680e44939ce4ae1c01bf87f6221e49;keep_landing=1&sb_price_type=total&",
                "http://www.4399.com",
                "http://www.iqiyi.com",
                "https://www.iqiyi.com",
                "https://tieba.baidu.com/index.html",
                "https://weibo.com",
                "https://www.zhihu.com/signup?next=%2F",
                "https://blog.csdn.net/",
                "https://www.csdn.net",
                "https://www.mi.com",
                "https://www.huawei.com/",
                "https://www.microsoft.com/zh-cn/",
                "https://www.hao123.com",
                "https://www.so.com",
                "https://cn.bing.com/",
                "https://news.163.com",
                "https://www.guancha.cn",
                "http://moe.hao123.com"
        };


        WsNettyClient wsNettyClient = new WsNettyClient();

        Queue<HttpResponseTask> list = new ConcurrentLinkedQueue();

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            log.info("当前HttpResponse数量为：{}", WsChannelHttpRequestBodyBind.httpResponseBodyMap.size());
            Map<String, CountDownLatch> map = WsChannelHttpRequestBodyBind.countDownLatchMap;
            log.info("当前CountDownLatch数量为：{}", map.size());
            log.info("当前Channel的数量{}", WsChannelHttpRequestBodyBind.channelMap.size());
            log.info("当前ChannelString的数量{}", WsChannelHttpRequestBodyBind.channelsStringMap.size());
            log.info("当前限制器：{}", WsChannelHttpRequestBodyBind.semaphore.availablePermits());
            log.info("超时检测队列大小：{}", WsChannelHttpRequestBodyBind.delayQueue.size());
            /*Set<Map.Entry<String, CountDownLatch>> set = map.entrySet();
            Iterator<Map.Entry<String, CountDownLatch>> iterator = set.iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, CountDownLatch> entry = iterator.next();
                log.info("channel：{},countDownLatch数量：{},当前链接：{}", entry.getKey(), entry.getValue().getCount(), WsChannelHttpRequestBodyBind.httpRequestBodyMap.get(entry.getKey()).getUrl());
            }*/
        }, 10, 20, TimeUnit.SECONDS);
        Random random = new Random();
        AtomicInteger successNum = new AtomicInteger(0);
        AtomicInteger errorNum = new AtomicInteger(0);
        CountDownLatch countDownLatch = new CountDownLatch(10000);
        Semaphore semaphore = new Semaphore(200);
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        for (int i = 0; i < 10000; i++) {
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            int i1 = i;
            int k = random.nextInt(strs.length);
            String str = strs[k];
            //System.out.println(str);
            HttpRequestBody httpRequestBody = HttpRequestBody.createHttpRequestBody();
            httpRequestBody.setUrl(str)
                    .setMethod("GET")
                    .setGZIP(true)
                    .setUseChunked(true);
            //.addRequestProperty("Range","bytes=512000-5120000");
            httpRequestBody.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36");
            HttpResponseTask httpResponseTask = httpRequestBody.nettyBuild();
            executorService.execute(() -> {

                list.add(httpResponseTask);
                try {
                    HttpResponseBody httpResponseBody = httpResponseTask.call();
                    if (httpResponseBody != null) {
                        log.info(httpResponseBody.getCode() + httpResponseBody.getHttpVersion());
                        //log.info(httpResponseBody.getHeaderProperty("Content-Length").get(0));
                        //log.info(httpResponseBody.getResponseBodyToString());
                        if (httpResponseBody.getCode() == 200) {
                            System.out.println(httpResponseBody.getContentType());
                            WsImageUtils.byteToFile(httpResponseBody.getReturnBytes(), httpRequestBody.getUri().getHost() + i1, "txt", "D:/网页/1/");
                        } else {
                            WsImageUtils.byteToFile(httpResponseBody.getErrorReturnBytes(), httpRequestBody.getUri().getHost() + i1, "txt", "D:/网页/1/");
                        }

                        successNum.getAndAdd(1);
                    } else {
                        errorNum.getAndAdd(1);
                    }

                    //log.info(httpResponseBody.getResponseBodyToString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                countDownLatch.countDown();
                semaphore.release();
            });

           /* HttpResponseTask httpResponseTask = wsNettyClient.clientStart(httpRequestBody);
            try {
                HttpResponseBody httpResponseBody = httpResponseTask.call();
                log.info(httpResponseBody.getCode()+httpResponseBody.getHttpVersion());
                //log.info(new String(httpResponseBody.getReturnBytes()));
            }catch (Exception e){
                e.printStackTrace();
            }
            list.add(httpResponseTask);*/

        }
        /*try {
            Thread.sleep(20000);
        }catch (InterruptedException e){
            e.printStackTrace();
        }*/

        for (int i = 0; i < 0; i++) {
            //new Thread(()->{
            int k = random.nextInt(8);
            String str = strs[k];
            System.out.println(str);
            HttpRequestBody httpRequestBody = HttpRequestBody.createHttpRequestBody();
            httpRequestBody.setUrl(str).setMethod("GET");
            HttpResponseTask httpResponseTask = WsNettyClient.clientStart(httpRequestBody);
            list.add(httpResponseTask);
            try {
                HttpResponseBody httpResponseBody = httpResponseTask.call();
                if (httpResponseBody != null) {
                    log.info(httpResponseBody.getCode() + httpResponseBody.getHttpVersion());
                }
                log.info(new String(httpResponseBody.getReturnBytes()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            countDownLatch.countDown();
            //}).start();

           /* HttpResponseTask httpResponseTask = wsNettyClient.clientStart(httpRequestBody);
            try {
                HttpResponseBody httpResponseBody = httpResponseTask.call();
                log.info(httpResponseBody.getCode()+httpResponseBody.getHttpVersion());
                //log.info(new String(httpResponseBody.getReturnBytes()));
            }catch (Exception e){
                e.printStackTrace();
            }
            list.add(httpResponseTask);*/

        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        /*int i = 0;
        list.parallelStream().forEach(httpResponseTask -> {
            try {
                HttpResponseBody httpResponseBody = httpResponseTask.call();
                log.info(httpResponseBody.getCode()+httpResponseBody.getHttpVersion());
                log.info(JSON.toJSONString(httpResponseBody.getHeaders()));
            }catch (Exception e){
                e.printStackTrace();
            }
        });*/


        long endTime = System.currentTimeMillis();
        log.info("执行完成，共花费：{}毫秒,其中成功：{}，失败：{},占用线程数为：{}", endTime - startTime, successNum.get(), errorNum.get(), WsChannelHttpRequestBodyBind.semaphore.availablePermits());
        //close();
        /*wsNettyClient.clientStart(httpRequestBody);
        endTime = System.currentTimeMillis();
        System.out.println(endTime - startTime);
        wsNettyClient.clientStart(httpRequestBody);
        endTime = System.currentTimeMillis();
        System.out.println(endTime - startTime);*/

        //wsNettyClient.clientStart(httpRequestBody);

    }


    public static HttpResponseTask clientStart(HttpRequestBody httpRequestBody) {
        //long startTime = System.currentTimeMillis();
        final String id = UUID.randomUUID().toString();
        WsChannelHttpRequestBodyBind.createCountDownLatch(id);

        //long endTiem = System.currentTimeMillis();
        //log.info("创建ID并添加countdownlatch花费的时间为：{}", endTiem - startTime);
        clientStart(httpRequestBody, id);
        return new HttpResponseTask(id);
    }

    public static String clientStart(HttpRequestBody httpRequestBody, final String id) {
        atomicInteger1.getAndAdd(1);
        //final String id = UUID.randomUUID().toString();
        //WsChannelHttpRequestBodyBind.createCountDownLatch(id);
        // long startTime = System.currentTimeMillis();

        WsChannelHttpRequestBodyBind.channelAndHttpRequestBodyBind(id, httpRequestBody);
        try {
            WsChannelHttpRequestBodyBind.semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executor.execute(() -> {
            try {

                Channel channel = WsChannelHttpRequestBodyBind.getChannelString(httpRequestBody.getUri().getHost() + ":" + httpRequestBody.getPort());
                long endTime = System.currentTimeMillis();
                //log.info("执行clientStart所花费的时间为：{}", endTime - startTime);
                if (channel != null && channel.isActive()) {
                    //log.info("使用已缓存的channel{}",httpRequestBody.getUrl());
                    NettyHttpClientResponseHandler nettyHttpClientResponseHandler = (NettyHttpClientResponseHandler) channel.pipeline().get("http-server");
                    nettyHttpClientResponseHandler.setId(id);
                    nettyHttpClientResponseHandler.setHttpRequestBody(httpRequestBody);
                    WsNettyClientUtils.sendHttpRequest(channel, httpRequestBody, id);
                } else {
                    //log.info("使用了新连接");

                    Bootstrap bootstrap = new Bootstrap();
                    bootstrap.group(workEventExecutors);
                    bootstrap.channel(NioSocketChannel.class);
                    bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
                    bootstrap.handler(new LoggingHandler(LogLevel.DEBUG));
                    ChannelFuture channelFuture = null;
                    bootstrap.handler(new HttpInitializer(httpRequestBody, id));
                    channelFuture = bootstrap.connect(httpRequestBody.getUri().getHost(), httpRequestBody.getPort());
                    channelFuture.channel().closeFuture().sync().addListener(new GenericFutureListener<Future<? super Void>>() {
                        @Override
                        public void operationComplete(Future<? super Void> future) throws Exception {
                            if (future.isSuccess()) {
                                log.info("关闭成功");
                            }
                        }
                    });
                    atomicInteger.getAndAdd(1);
                    log.info("{}结束{}", id, httpRequestBody.getUrl());
                }

            } catch (Exception e) {
                WsChannelHttpRequestBodyBind.semaphore.release();
                WsChannelHttpRequestBodyBind.countDownCountDownLatch(id);
                e.printStackTrace();
            }/*finally {
                workEventExecutors.shutdownGracefully();
            }*/
            long endTime = System.currentTimeMillis();

        });
        return id;
    }

    public static void close() {
        workEventExecutors.shutdownGracefully();
        executor.shutdownNow();
        WsChannelHttpRequestBodyBind.executorService.shutdownNow();

    }

}
