package cn.katoumegumi.java.vertx.http;

import cn.katoumegumi.java.http.client.model.HttpRequestBody;
import cn.katoumegumi.java.http.client.model.HttpResponseBody;
import cn.katoumegumi.java.http.client.model.HttpResponseTask;
import cn.katoumegumi.java.http.client.model.WsRequestProperty;
import com.alibaba.fastjson.JSON;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

public class WsVertxClient {

    public static Vertx vertx;
    public static WebClient webClient;

    static {
        VertxOptions vertxOptions = new VertxOptions();
        vertxOptions.setWorkerPoolSize(50);
        vertx = Vertx.vertx(vertxOptions);
        WebClientOptions webClientOptions = new WebClientOptions();
        webClientOptions.setUserAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36");
        webClient = WebClient.create(vertx, webClientOptions);
    }


    public static void main(String[] args) {


        String strs[] = new String[]{
                "https://www.baidu.com",
                "https://www.bilibili.com",
                "http://www.baidu.com",
                "http://www.bilibili.com",
                "https://www.sina.com.cn",
                "https://www.qq.com",
                "https://www.360.cn",
                "https://www.fishmaimai.com",
                "https://www.jd.com/?cu=true",
                "https://www.tmall.com/?ali_trackid=2:mm_26632322_6858406_70736499:1562211803_219_1783123944",
                "http://www.ifeng.com",
                "https://www.suning.com",
                "https://www.autohome.com.cn/beijing/",
                "https://www.taobao.com",
                "http://www.eastmoney.com",
                "https://hf.58.com/?spm=b-31580022738699-me-f-862.mingzhan",
                "https://www.ctrip.com",
                "https://www.douban.com",
                "https://www.booking.com/index.zh-cn.html?aid=1337411;label=bai408jc-1DCAEoggI46AdIM1gDaDGIAQGYASu4ARfIAQzYAQPoAQH4AQSIAgGoAgO4At3s9egFwAIB;sid=30680e44939ce4ae1c01bf87f6221e49;keep_landing=1&sb_price_type=total&",
                "http://www.4399.com",
                "http://www.iqiyi.com",
                "https://www.iqiyi.com",
                "https://tieba.baidu.com/index.html",
                "https://weibo.com",
                "https://www.zhihu.com/signup?next=%2F",
                "https://blog.csdn.net/",
                "https://www.csdn.net",
                "https://www.mi.com",
                "https://www.huawei.com/",
                "https://www.microsoft.com/zh-cn/",
                "https://www.hao123.com",
                "https://www.so.com",
                "https://cn.bing.com/",
                "https://news.163.com",
                "https://www.guancha.cn",
                "http://moe.hao123.com"
        };


        Executor executor = Executors.newCachedThreadPool();
        for (int i = 0; i < 10; i++) {
            executor.execute(() -> {
                Random random = new Random();
                int k = random.nextInt(strs.length);
                String str = strs[k];
                HttpRequestBody httpRequestBody = HttpRequestBody.createHttpRequestBody().setUrl(str);
                httpRequestBody.setPkcsPath("");
                httpRequestBody.setPkcsPassword("");
                HttpResponseTask httpResponseTask = clientStart(httpRequestBody);
                try {
                    HttpResponseBody httpResponseBody = httpResponseTask.call();
                    System.out.println(JSON.toJSONString(httpResponseBody.getHeaders()));
                } catch (TimeoutException e) {
                    e.printStackTrace();
                }
            });

        }


    }


    public static HttpResponseTask clientStart(HttpRequestBody httpRequestBody) {
        boolean isPrivate = false;
        final WebClient webClient;
        if (httpRequestBody.getPkcsPath() != null) {
            WebClientOptions webClientOptions = new WebClientOptions();
           /* PfxOptions pfxOptions = new PfxOptions();
            pfxOptions.setPath(httpRequestBody.getPkcsPath());
            pfxOptions.setPassword(httpRequestBody.getPkcsPassword());
            webClientOptions.setPfxKeyCertOptions(pfxOptions);*/
            webClient = WebClient.create(vertx, webClientOptions);
        } else {
            webClient = WsVertxClient.webClient;
        }

        CountDownLatch countDownLatch = new CountDownLatch(1);
        HttpResponseTask httpResponseTask = new HttpResponseTask(countDownLatch);
        List<WsRequestProperty> list = httpRequestBody.getRequestProperty();
        MultiMap multiMap = MultiMap.caseInsensitiveMultiMap();
        for (WsRequestProperty wsRequestProperty : list) {
            multiMap.add(wsRequestProperty.getKey(), wsRequestProperty.getValue());
        }
        byte bytes[] = httpRequestBody.getByteHttpRequestBody();
        if (bytes == null) {
            bytes = new byte[0];
        }
        webClient.request(getHttpMethod(httpRequestBody.getMethod()), httpRequestBody.getPort(), httpRequestBody.getUri().getHost(), httpRequestBody.getUri().getPath())
                .ssl(httpRequestBody.isHttps())
                .putHeaders(multiMap).sendBuffer(Buffer.buffer(bytes), new Handler<AsyncResult<HttpResponse<Buffer>>>() {
            @Override
            public void handle(AsyncResult<HttpResponse<Buffer>> event) {
                if (event.succeeded()) {
                    HttpResponse<Buffer> httpResponse = event.result();
                    MultiMap multiMap1 = httpResponse.headers();
                    List<Map.Entry<String, String>> list1 = multiMap1.entries();
                    Map<String, List<String>> map = new HashMap<>();
                    for (Map.Entry<String, String> entry : list1) {
                        map.put(entry.getKey(), new ArrayList<>(Arrays.asList(entry.getValue().split(","))));
                    }
                    HttpResponseBody httpResponseBody = HttpResponseBody.createHttpResponseBody(httpResponse.statusCode(), httpResponse.body().getBytes(), map);
                    httpResponseBody.setUrl(httpRequestBody.getUrl());
                    httpResponseBody.setHttpVersion(httpResponse.version().name());
                    httpResponseBody.build();
                    httpResponseTask.setHttpResponseBody(httpResponseBody);
                    countDownLatch.countDown();
                } else {
                    event.cause().printStackTrace();
                    countDownLatch.countDown();
                }
                if (isPrivate) {
                    webClient.close();
                }

            }
        });
        return httpResponseTask;
    }

    public static HttpMethod getHttpMethod(String method) {
        method = method.toUpperCase();
        switch (method) {
            case "GET":
                return HttpMethod.GET;
            case "POST":
                return HttpMethod.POST;
            case "PUT":
                return HttpMethod.PUT;
            case "CONNECT":
                return HttpMethod.CONNECT;
            case "DELETE":
                return HttpMethod.DELETE;
            case "HEAD":
                return HttpMethod.HEAD;
            case "OPTIONS":
                return HttpMethod.OPTIONS;
            case "PATCH":
                return HttpMethod.PATCH;
            default:
                return HttpMethod.GET;
        }
    }


}
