package cn.katoumegumi.java.vertx.test;


import com.alibaba.fastjson.JSON;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;

public class VertTest {


    public static void main(String[] args) {
        VertxOptions vertxOptions = new VertxOptions();
        vertxOptions.setWorkerPoolSize(50);
        Vertx vertx = Vertx.vertx(vertxOptions);
        WebClient webClient = WebClient.create(vertx);
        HttpRequest<Buffer> httpRequest = webClient.get(443,"www.baidu.com","/");
        httpRequest.method(HttpMethod.GET);
        httpRequest.ssl(true);
        httpRequest.putHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36");
        Buffer buffer = Buffer.buffer();
        httpRequest.sendBuffer(buffer,new Handler<AsyncResult<HttpResponse<Buffer>>>() {
            @Override
            public void handle(AsyncResult<HttpResponse<Buffer>> event) {
                if(event.succeeded()){
                    HttpResponse<Buffer> httpResponse = event.result();
                    System.out.println(httpResponse.statusCode());
                    MultiMap multiMap = httpResponse.headers();
                    System.out.println(JSON.toJSONString(multiMap.entries()));
                    Buffer buffer = httpResponse.body();
                    System.out.println(buffer.toString());
                }else {
                    System.out.println(event.cause().getMessage());
                }
            }
        });
        webClient.close();

        //HttpServerOptions httpServerOptions = new HttpServerOptions();
        /*HttpServer httpServer = vertx.createHttpServer();
        Router router = Router.router(vertx);
        router.route("/").handler(new Handler<RoutingContext>() {
            @Override
            public void handle(RoutingContext event) {
                HttpServerResponse httpServerResponse = event.response();
                httpServerResponse.putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
                httpServerResponse.setChunked(true);
                httpServerResponse.write("你好世界");
                httpServerResponse.end();
            }
        });
        httpServer.requestHandler(router).listen(1234);*/
    }


}
