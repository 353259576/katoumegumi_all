package cn.katoumegumi.java.vertx.sql.utils;

import cn.katoumegumi.java.common.WsFileUtils;
import cn.katoumegumi.java.common.WsImageUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.buffer.impl.BufferImpl;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class NetFileUtils {

    private static final Vertx vertx = Vertx.vertx();

    private static final WebClient webClient = WebClient.create(vertx);

    public static void main(String[] args) {

        /*List<FileCallable> fileCallableList = new ArrayList<>();

        for(int i = 0; i < 10; i++){
            //FileCallable fileCallable = getNetFile("https://i0.hdslb.com/bfs/vc/8e084d67aa18ed9c42dce043e06e16b79cbb50ef.png");
            FileCallable fileCallable = getNetFile("https://i0.hdslb.com/bfs/vc/8e084d67aa18ed9c42dce043e06e16b79cbb50ef.png");
            fileCallableList.add(fileCallable);
        }
        int i = 0;
        for(FileCallable fileCallable:fileCallableList){
            i++;
            WsImageUtils.byteToFile(getByteArrayOutputStream(fileCallable,false).toByteArray(),"测试"+i,"jpg","D:\\网页\\");
        }*/

        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT,new ByteArrayInputStream(getByteArrayOutputStream(getNetFile("http://img.kometl.com/1.ttf"),true).toByteArray()) );
            System.out.println(font.getName());
        } catch (FontFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static FileCallable getNetFile(String urlPath){
        FileCallable fileCallable = new FileCallable();
        webClient.getAbs(urlPath).send().onSuccess(response -> {
            if(response.statusCode() == 200 || response.statusCode() == 302){
                Buffer buffer = response.body();
                buffer.getByteBuf();
                byte[] bytes = buffer.getBytes();
                fileCallable.setFile(bytes);
            }else {
                fileCallable.error();
            }
        }).onFailure(throwable -> {
            fileCallable.error();
            throwable.printStackTrace();
        });
        return fileCallable;
    }

    public static ByteArrayOutputStream getByteArrayOutputStream(FileCallable fileCallable,boolean clearBuf){
        try {
            ByteBuf byteBuf = fileCallable.call();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int byteLength = 1024;
            byte[] bytes = new byte[byteLength];
            int length = 0;
            while ((length = byteBuf.readableBytes()) > 0){
                byteBuf.readBytes(bytes, 0, Math.min(length, byteLength));
                byteArrayOutputStream.write(bytes,0, Math.min(length, byteLength));
            }
            if(!clearBuf){
                byteBuf.resetReaderIndex();
            }else {
                byteBuf.clear();
            }
            return byteArrayOutputStream;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    public static class FileCallable implements Callable<ByteBuf>{

        private final CountDownLatch countDownLatch = new CountDownLatch(1);

        private volatile ByteBuf byteBuf;

        /**
         * Computes a result, or throws an exception if unable to do so.
         *
         * @return computed result
         * @throws Exception if unable to compute a result
         */
        @Override
        public ByteBuf call() throws Exception {
            boolean k = countDownLatch.await(10,TimeUnit.MINUTES);
            if(k && byteBuf != null){
                return byteBuf;
            }else {
                throw new RuntimeException("获取失败");
            }

        }

        public void setFile(byte[] bytes){
            ByteBuf byteBuf = Unpooled.directBuffer();
            byteBuf.writeBytes(bytes);
            this.byteBuf = byteBuf;
            countDownLatch.countDown();
        }

        public void error(){
            countDownLatch.countDown();
        }
    }


}
