package com.ws.java.http.client.model;

import com.ws.java.http.client.processor.WsChannelHttpRequestBodyBind;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

@Slf4j
@Data
public class HttpResponseTask implements Callable<HttpResponseBody> {
    private String channelId;
    private boolean isNetty = true;
    private CountDownLatch countDownLatch;

    private HttpResponseBody httpResponseBody;

    public HttpResponseTask(String channelId){
        this.channelId = channelId;
    }
    public HttpResponseTask(CountDownLatch countDownLatch){
        this.countDownLatch = countDownLatch;
        this.isNetty = false;
    }

    @Override
    public HttpResponseBody call() throws TimeoutException{
        if (isNetty){
            HttpResponseBody httpResponseBody = WsChannelHttpRequestBodyBind.takeHttpResponseBody(channelId);
            if(httpResponseBody != null){
                httpResponseBody.build();
            }else {
                throw new TimeoutException("获取失败");
            }
            return httpResponseBody;
        }else {
            try {
                countDownLatch.await();
            }catch (InterruptedException e){
                throw new TimeoutException("请求被终端");
            }
            if(httpResponseBody == null){
                throw new RuntimeException("获取失败");
            }
            return httpResponseBody;
        }

    }

}
