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


    public static void main(String[] args) {
        ChannelTimeoutEntry c1 = new ChannelTimeoutEntry();
        c1.setId("1");
        c1.setExpirationTime(System.currentTimeMillis() + 6000);
        ChannelTimeoutEntry c2 = new ChannelTimeoutEntry();
        c2.setId("2");
        c2.setExpirationTime(System.currentTimeMillis() + 5000);
        ChannelTimeoutEntry c3 = new ChannelTimeoutEntry();
        c3.setId("3");
        c3.setExpirationTime(System.currentTimeMillis() + 7000);
        ChannelTimeoutEntry c4 = new ChannelTimeoutEntry();
        c4.setId("4");
        c4.setExpirationTime(System.currentTimeMillis() + 3000);
        ChannelTimeoutEntry c5 = new ChannelTimeoutEntry();
        c5.setId("5");
        c5.setExpirationTime(System.currentTimeMillis() + 2000);
        ChannelTimeoutEntry c6 = new ChannelTimeoutEntry();
        c6.setId("6");
        c6.setExpirationTime(System.currentTimeMillis() + 1000);
        DelayQueue<ChannelTimeoutEntry> delayQueue = new DelayQueue<>();
        delayQueue.put(c1);
        delayQueue.put(c2);
        delayQueue.put(c3);
        delayQueue.put(c4);
        delayQueue.put(c5);
        delayQueue.put(c6);
        while (delayQueue.size() != 0) {
            try {
                System.out.println(delayQueue.take().getId());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }


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
