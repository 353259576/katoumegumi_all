package cn.katoumegumi.java.http.client.model;

import io.netty.channel.Channel;

import java.util.Objects;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class ChannelTimeoutEntry implements Delayed {

    private String id;

    private Long expirationTime;

    private int count;

    private Channel channel;

    private boolean isTimeout;

    private HttpRequestBody httpRequestBody;

    @Override
    public long getDelay(TimeUnit unit) {
        long l = unit.convert(expirationTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        return l;
        //return createTime + expirationTime;
    }

    @Override
    public int compareTo(Delayed o) {
        long l = getDelay(TimeUnit.SECONDS) - o.getDelay(TimeUnit.SECONDS);
        if (l > 0) {
            return 1;
        } else if (l < 0) {
            return -1;
        } else {
            return 0;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChannelTimeoutEntry that = (ChannelTimeoutEntry) o;
        return Objects.equals(id, that.id);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(Long expirationTime) {
        this.expirationTime = expirationTime;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public boolean isTimeout() {
        return isTimeout;
    }

    public void setTimeout(boolean timeout) {
        isTimeout = timeout;
    }

    public HttpRequestBody getHttpRequestBody() {
        return httpRequestBody;
    }

    public void setHttpRequestBody(HttpRequestBody httpRequestBody) {
        this.httpRequestBody = httpRequestBody;
    }
}
