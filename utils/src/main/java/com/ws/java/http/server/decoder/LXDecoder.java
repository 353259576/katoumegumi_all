package com.ws.java.http.server.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.ByteBuffer;
import java.util.List;

public class LXDecoder extends ByteToMessageDecoder {
    private volatile Integer state;
    private volatile Integer length;

    public static void main(String[] args) {
        Integer k =  125;
        char c = '{';
        System.out.println((byte)c);
        byte b = 33;
        System.out.println((char) b);
        byte bytes[] = new byte[4];
        bytes[0] = 0;
        bytes[1] = 33;
        bytes[2] = 0;
        bytes[3] = 123;
        int length = bytes[3] << 24 | bytes[2] << 16 | bytes[1] << 8 | bytes[0];
        System.out.println(length);
        ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes);
        System.out.println((long)byteBuf.getShort(0));
        ByteBuffer byteBuffer = byteBuf.nioBuffer();
        System.out.println(byteBuffer.getShort());
    }



    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        //获取第一个长度
        if(state == null){
            int num = byteBuf.readableBytes();
            //不足四个字节忽略
            if(num < 4){
                return;
            }else {
                //读取长度
                byte bytes[] = new byte[4];
                byteBuf.readBytes(bytes,0,4);
                length = bytes[3] << 24 | bytes[2] << 16 | bytes[1] << 8 | bytes[0];
                state = 1;
            }
        }

        //JsonObjectDecoder
    }
}
