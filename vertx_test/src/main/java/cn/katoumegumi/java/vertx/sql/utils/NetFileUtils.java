package cn.katoumegumi.java.vertx.sql.utils;

import cn.katoumegumi.java.common.WsFileUtils;
import cn.katoumegumi.java.common.WsImageUtils;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;

public class NetFileUtils {

    private static final Vertx vertx = Vertx.vertx();

    private static final WebClient webClient = WebClient.create(vertx);

    public static void main(String[] args) {
        getNetFile("https://i0.hdslb.com/bfs/vc/8e084d67aa18ed9c42dce043e06e16b79cbb50ef.png");
    }

    public static void getNetFile(String urlPath){

        webClient.getAbs(urlPath).send(event -> {
            if(event.succeeded()){
                HttpResponse<Buffer> response = event.result();
                if(response.statusCode() == 200 || response.statusCode() == 302){
                    Buffer buffer = response.body();
                    byte[] bytes = buffer.getBytes();
                    WsImageUtils.byteToFile(bytes,"测试","jpg","D:\\网页\\");
                }else {
                    System.out.println("获取失败"+response.statusCode());
                }
            }
        });


    }



}
