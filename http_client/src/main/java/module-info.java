module http.client {
    requires com.google.gson;
    requires io.netty.buffer;
    requires io.netty.codec;
    requires io.netty.codec.http;
    requires io.netty.codec.http2;
    requires io.netty.common;
    requires io.netty.handler;
    requires io.netty.transport;
    requires io.netty.transport.rxtx;
    requires reactor.core;
    requires common.utils;
    requires org.slf4j;

    exports cn.katoumegumi.java.http.client;
    exports cn.katoumegumi.java.http.client.model;
    exports cn.katoumegumi.java.http.client.utils;
    exports cn.katoumegumi.java.http.utils;
}