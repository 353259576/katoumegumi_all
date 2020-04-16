package cn.katoumegumi.java.http.client.model;

import cn.katoumegumi.java.http.client.processor.WsChannelHttpRequestBodyBind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;


public class HttpResponseTask implements Callable<HttpResponseBody> {

    private static final Logger log = LoggerFactory.getLogger(HttpResponseTask.class);

    private String channelId;
    private boolean isNetty = true;
    private CountDownLatch countDownLatch;

    private HttpResponseBody httpResponseBody;

    public HttpResponseTask(String channelId) {
        this.channelId = channelId;
    }

    public HttpResponseTask(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
        this.isNetty = false;
    }

    @Override
    public HttpResponseBody call() throws TimeoutException {
        if (isNetty) {
            HttpResponseBody httpResponseBody = WsChannelHttpRequestBodyBind.takeHttpResponseBody(channelId);
            if (httpResponseBody != null) {
                httpResponseBody.build();
            } else {
                throw new TimeoutException("获取失败");
            }
            return httpResponseBody;
        } else {
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                throw new TimeoutException("请求被终端");
            }
            if (httpResponseBody == null) {
                throw new RuntimeException("获取失败");
            }
            return httpResponseBody;
        }

    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public boolean isNetty() {
        return isNetty;
    }

    public void setNetty(boolean netty) {
        isNetty = netty;
    }

    public CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }

    public void setCountDownLatch(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    public HttpResponseBody getHttpResponseBody() {
        return httpResponseBody;
    }

    public void setHttpResponseBody(HttpResponseBody httpResponseBody) {
        this.httpResponseBody = httpResponseBody;
    }
}
