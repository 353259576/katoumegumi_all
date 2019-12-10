package com.ws.java.http.client.processor;

import com.ws.java.http.client.WsNettyClient;
import com.ws.java.http.client.handler.HttpResponseHandler;
import com.ws.java.http.client.model.ChannelTimeoutEntry;
import com.ws.java.http.client.model.HttpRequestBody;
import com.ws.java.http.client.model.HttpResponseBody;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

@Slf4j
public class WsChannelHttpRequestBodyBind {

    public static volatile Map<String, HttpRequestBody> httpRequestBodyMap = new ConcurrentHashMap<>();//id与request的绑定

    public static volatile ConcurrentMap<String,CountDownLatch> countDownLatchMap = new ConcurrentHashMap<>();//id与门闩的绑定

    public static volatile Map<String, HttpResponseBody> httpResponseBodyMap = new ConcurrentHashMap<>();//id与response的绑定

    public static volatile Map<String,ConcurrentLinkedQueue<String>> channelsStringMap = new ConcurrentHashMap<>();//储存可以复用的通道id

    public static volatile Map<String,Channel> notCloseChannel = new ConcurrentHashMap<>();

    public static volatile Map<String, ByteBuf> stringByteBufMap = new ConcurrentHashMap<>();

    public static volatile Map<String,Channel> channelMap = new ConcurrentHashMap<>();//可以被重新使用的通道

    //public static volatile Map<String,HttpRequestBody> stringHttpRequestBodyMap = new ConcurrentHashMap<>();

    public static volatile Map<String, ChannelTimeoutEntry> idChannelTimeoutEntryMap = new ConcurrentHashMap<>();

    public static DelayQueue<ChannelTimeoutEntry> delayQueue = new DelayQueue<>();//延时队列

    public static ExecutorService executorService = Executors.newSingleThreadExecutor();//延时队列处理线程池

    public static volatile Semaphore semaphore = new Semaphore(200);//处理数量限制器

    public static volatile Map<String,String> idStreamIdMap = new ConcurrentHashMap<>();

    public static volatile Map<String,Channel> http2ChannelMap = new ConcurrentHashMap<>();

    public static volatile Map<String, HttpResponseHandler> stringHttpResponseHandlerMap = new ConcurrentHashMap<>();

    static {
        executorService.submit(()->{
            while (true){
                ChannelTimeoutEntry channelTimeoutEntry = delayQueue.take();
                channelTimeoutEntry.setTimeout(true);
                //log.info("{}开始重连，{}",channelTimeoutEntry.getId(),channelTimeoutEntry.getHttpRequestBody().getUrl());
                if(idChannelTimeoutEntryMap.get(channelTimeoutEntry.getId()) != null) {
                    new Thread(() -> {
                        HttpRequestBody httpRequestBody = channelTimeoutEntry.getHttpRequestBody();
                        Channel channel = channelTimeoutEntry.getChannel();
                        log.info("通道是否打开：{}，通道是否活动：{}，通道是否注册：{}", channel.isOpen(), channel.isActive(), channel.isRegistered());
                        WsChannelHttpRequestBodyBind.removeChannelAndHttpRequestBodyBind(channelTimeoutEntry.getId());
                        WsChannelHttpRequestBodyBind.removeResumableChannel(httpRequestBody,channelTimeoutEntry.getChannel());
                        WsChannelHttpRequestBodyBind.reconnectLink(httpRequestBody,channelTimeoutEntry.getId(),channelTimeoutEntry.getChannel());
                        channel.close();
                        log.info("{}超时关闭{}", channelTimeoutEntry.getId(), httpRequestBody.getUrl());
                    }).start();
                }
            }
        });
    }


    public static void channelAndHttpRequestBodyBind(String channelId, HttpRequestBody httpRequestBody){
        //log.info("添加一个request成功");
        httpRequestBodyMap.put(channelId,httpRequestBody);
    }

    public static void removeChannelAndHttpRequestBodyBind(String channelId){
        httpRequestBodyMap.remove(channelId);
    }


    public static void putHttpResponseBody(String channelId, HttpResponseBody httpResponseBody){
        HttpRequestBody httpRequestBody = httpRequestBodyMap.get(channelId);
        if(httpRequestBody != null){
            httpResponseBodyMap.put(channelId,httpResponseBody);
            countDownCountDownLatch(channelId);
        }
    }

    public static void countDownCountDownLatch(String channelId){
        CountDownLatch countDownLatch = null;
        int i = 0;
        while (countDownLatch == null){
            countDownLatch = countDownLatchMap.get(channelId);

            if(countDownLatch == null){
                i++;
                if(i > 10){
                    break;
                }
                try {
                    Thread.sleep(100);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }

        }
        if(countDownLatch != null){
            countDownLatch.countDown();
        }
    }

    public static void createCountDownLatch(String channelId){
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatchMap.put(channelId,countDownLatch);
    }


    public static HttpResponseBody takeHttpResponseBody(String channelId){
        CountDownLatch countDownLatch = null;
        while (countDownLatch == null){
            countDownLatch = countDownLatchMap.get(channelId);
            if(countDownLatch == null){
                try {
                    log.info("线程沉睡");
                    Thread.sleep(10);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }

        try {
            countDownLatch.await();
            countDownLatchMap.remove(channelId);
            return httpResponseBodyMap.remove(channelId);
        }catch (InterruptedException e){
            e.printStackTrace();
            return null;
        }

    }


    public static void putChannelString(String url,String id){
        ConcurrentLinkedQueue<String> queue = channelsStringMap.get(url);
        if(queue == null){
            queue = new ConcurrentLinkedQueue<>();
            channelsStringMap.put(url,queue);
        }
        queue.add(id);
    }

    public static synchronized Channel getChannelString(String url){
        ConcurrentLinkedQueue<String> queue = channelsStringMap.get(url);
        if(queue == null){
            return null;
        }
        String id = queue.poll();
        if(id == null){
            return null;
        }
        Channel channel = channelMap.get(id);
        if(channel != null){
            channelMap.remove(id);
        }
        return channel;

    }


    public static void putChannel(String id,Channel channel){
        channelMap.put(id,channel);
    }

    public static Channel getChannel(String id){
        return channelMap.get(id);
    }


    /**
     * 插入一个可回收的channel
     * @param httpRequestBody
     * @param channel
     */
    public static void addResumableChannel(HttpRequestBody httpRequestBody, Channel channel){
        String channelId = channel.id().asLongText();
        String searchName = httpRequestBody.getUri().getHost()+":"+httpRequestBody.getPort();
        Queue<String> queue = WsChannelHttpRequestBodyBind.channelsStringMap.get(searchName);
        if(queue != null && queue.size() > 1&& WsChannelHttpRequestBodyBind.channelMap.size() > 100){
            //log.info("{}超过阈值，不缓存当前的channel",searchName);
            channel.close();
            return;
        }
        WsChannelHttpRequestBodyBind.putChannel(channelId,channel);
        WsChannelHttpRequestBodyBind.putChannelString(searchName,channelId);
    }

    /**
     * channel关闭，在可重复利用列表里删除该条数据
     * @param httpRequestBody
     */
    public static void removeResumableChannel(HttpRequestBody httpRequestBody, Channel channel){
        String channelId = channel.id().asLongText();
        if(channelMap.containsKey(channelId)){
            channelMap.remove(channelId);
        }
        ConcurrentLinkedQueue<String> queue = WsChannelHttpRequestBodyBind.channelsStringMap.get(httpRequestBody.getUri().getHost()+":"+httpRequestBody.getPort());
        if(queue != null){
            /*WsChannelHttpRequestBodyBind.channelsStringMap.remove(httpRequestBody.getUri().getHost()+":"+httpRequestBody.getPort());
            while (!queue.isEmpty()) {
                WsChannelHttpRequestBodyBind.channelMap.remove(queue.poll());
            }*/
            if(queue.contains(channelId)){
                queue.remove(channelId);
            }
            if(queue.size() == 0){
                WsChannelHttpRequestBodyBind.channelsStringMap.remove(httpRequestBody.getUri().getHost()+":"+httpRequestBody.getPort());
            }
        }

    }

    public static void reconnectLink(HttpRequestBody httpRequestBody, String id, Channel channel){
        CountDownLatch countDownLatch = WsChannelHttpRequestBodyBind.countDownLatchMap.get(id);
        if(countDownLatch != null){
            if(countDownLatch.getCount() > 0){
                semaphore.release();
                if(WsChannelHttpRequestBodyBind.stringByteBufMap.containsKey(id)){
                    ByteBuf byteBuf = WsChannelHttpRequestBodyBind.stringByteBufMap.remove(id);
                    byteBuf.release();
                }
                ChannelTimeoutEntry channelTimeoutEntry = WsChannelHttpRequestBodyBind.idChannelTimeoutEntryMap.get(id);
                if(channelTimeoutEntry != null){
                    //WsChannelHttpRequestBodyBind.idChannelTimeoutEntryMap.remove(id);
                    if(channelTimeoutEntry.getCount() > httpRequestBody.getRetryNuumber()){
                        countDownLatch.countDown();
                        WsChannelHttpRequestBodyBind.httpResponseBodyMap.remove(id);
                        WsChannelHttpRequestBodyBind.idChannelTimeoutEntryMap.remove(id);
                        if(channelTimeoutEntry.isTimeout()){
                            log.info("Channel:{}连接超时，解除countdownlatch，链接为：{}",channel.id().asLongText(),httpRequestBody.getUrl());
                        }else {
                            log.info("Channel:{}连接错误，解除countdownlatch，链接为：{}",channel.id().asLongText(),httpRequestBody.getUrl());
                        }

                    }else {
                        channelTimeoutEntry.setCount(channelTimeoutEntry.getCount() + 1);
                        if(channelTimeoutEntry.isTimeout()){
                            log.info("超时重连");
                        }else {
                            log.info("错误重连");
                        }

                        WsNettyClient.clientStart(httpRequestBody,id);
                    }
                }else {
                    channelTimeoutEntry = new ChannelTimeoutEntry();
                    channelTimeoutEntry.setCount(1);
                    channelTimeoutEntry.setHttpRequestBody(httpRequestBody);
                    channelTimeoutEntry.setId(id);
                    channelTimeoutEntry.setChannel(channel);
                    channelTimeoutEntry.setTimeout(false);
                    WsChannelHttpRequestBodyBind.idChannelTimeoutEntryMap.put(id,channelTimeoutEntry);
                    log.info("错误重连");
                    WsNettyClient.clientStart(httpRequestBody,id);
                }

            }
        }
    }



    /**
     * 插入一个监控channel超时的记录
     * @param channel
     * @param httpRequestBody
     * @param id
     */
    public static void putChannelDelay(Channel channel, HttpRequestBody httpRequestBody, String id){
        ChannelTimeoutEntry channelTimeoutEntry = WsChannelHttpRequestBodyBind.idChannelTimeoutEntryMap.get(id);
        if(channelTimeoutEntry == null){
            channelTimeoutEntry = new ChannelTimeoutEntry();
            channelTimeoutEntry.setExpirationTime(System.currentTimeMillis()+httpRequestBody.getExpirationTime());
            channelTimeoutEntry.setChannel(channel);
            channelTimeoutEntry.setCount(1);
            channelTimeoutEntry.setId(id);
            channelTimeoutEntry.setHttpRequestBody(httpRequestBody);
            WsChannelHttpRequestBodyBind.idChannelTimeoutEntryMap.put(id,channelTimeoutEntry);
        }else {
            channelTimeoutEntry.setExpirationTime(System.currentTimeMillis()+httpRequestBody.getExpirationTime());
            channelTimeoutEntry.setChannel(channel);
            channelTimeoutEntry.setCount(channelTimeoutEntry.getCount() + 1);
            channelTimeoutEntry.setId(id);
            channelTimeoutEntry.setHttpRequestBody(httpRequestBody);
        }
        WsChannelHttpRequestBodyBind.delayQueue.put(channelTimeoutEntry);
        //WsChannelHttpRequestBodyBind.stringHttpRequestBodyMap.put(id,httpRequestBody);

    }

}
