package cn.katoumegumi.java.http.reptile;

import cn.katoumegumi.java.common.WsImageUtils;
import cn.katoumegumi.java.common.WsStringUtils;
import cn.katoumegumi.java.http.client.model.HttpRequestBody;
import cn.katoumegumi.java.http.client.model.HttpResponseBody;
import cn.katoumegumi.java.http.client.model.HttpResponseTask;

import java.util.UUID;
import java.util.concurrent.*;

public class HttpUrlRelolver {
    public static final String HTTP = "http";
    public static final String HTTPS = "https";

    public static volatile ConcurrentHashMap<String, Object> urls = new ConcurrentHashMap<>();
    public static volatile BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    public static Executor executor = Executors.newCachedThreadPool();


    public static void main(String[] args) throws Exception {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(20000);
                    System.out.println("待解析的连接：" + queue.size() + "条");
                    System.out.println("已完成解析的数据：" + urls.size() + "条");
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();
        queue.put("https://bcy.net");
        while (true) {
            String url = queue.take();
            executor.execute(() -> {
                if (urls.containsKey(url)) {
                    System.out.println(url + "已存在");
                    return;
                }
                urls.put(url, "");
                HttpRequestBody httpRequestBody = HttpRequestBody.createHttpRequestBody();
                httpRequestBody.setUrl(url)
                        .setMethod("GET")
                        .setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 8.0; Pixel 2 Build/OPD3.170816.012) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Mobile Safari/537.36");
                HttpResponseTask httpResponseTask = httpRequestBody.nettyBuild();
                try {
                    HttpResponseBody httpResponseBody = httpResponseTask.call();
                    if (httpResponseBody.getCode() == 200) {
                        System.out.println(httpResponseBody.getUrl() + "查询成功");
                        if (httpResponseBody.getContentType().contains("text")) {
                            String html = httpResponseBody.getResponseBodyToString();
                            if (WsStringUtils.isNotBlank(html)) {
                                getUrlFromHtml(html, httpResponseBody.getUrl());
                            }
                        } else if (httpResponseBody.getContentType().contains("image")) {
                            WsImageUtils.byteToFile(httpResponseBody.getReturnBytes(), UUID.randomUUID().toString(), "jpg", "F:\\图片\\");
                        }
                    } else {
                        System.out.println(httpResponseBody.getUrl() + "查询错误");
                        String html = httpResponseBody.getResponseBodyToString();
                        if (WsStringUtils.isNotBlank(html)) {
                            getUrlFromHtml(html, httpResponseBody.getUrl());
                        }
                    }
                } catch (TimeoutException e) {
                    e.printStackTrace();
                }
            });
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /*File file = FileUtils.createFile("F:\\网页\\首页 - 半次元 - ACG爱好者社区.html");
        ByteBuffer byteBuffer = WsStreamUtils.InputStreamToByteBuffer(new FileInputStream(file));
        byte bytes[] = new byte[byteBuffer.limit()];
        byteBuffer.get(bytes);
        byteBuffer.clear();
        String str = new String(bytes);
        getUrlFromHtml(str,"https://bcy.net");
        for(String url :queue){
            System.out.println(url);
        }*/
    }


    public static String compleHttpUrlString(String url, String pUrl) {
        url = WsStringUtils.decodeUnicode(url);
        String newUrl = url;
        if (url.startsWith(HTTP)) {
            newUrl = url;
        }
        if (url.startsWith("//")) {
            if (pUrl.startsWith(HTTPS)) {
                newUrl = HTTPS + ":" + url;
            } else {
                newUrl = HTTP + ":" + url;
            }
        }
        newUrl = newUrl.replaceAll("\\\\", "");
        return newUrl;

    }


    public static void getUrlFromHtml(String html, String url) {
        Integer length = html.length();
        boolean isUrl = false;
        char[] chars = html.toCharArray();
        StringBuffer stringBuffer = null;
        for (int i = 0; i < length; i++) {
            if (!isUrl) {
                if (chars[i] == 'h') {
                    if ("https".equals(html.substring(i, i + 5))) {
                        if (chars[i + 5] == ':') {
                            isUrl = true;
                            stringBuffer = new StringBuffer("https:");
                            i = i + 5;
                        }
                    } else if ("http:".equals(html.substring(i, i + 5))) {
                        isUrl = true;
                        stringBuffer = new StringBuffer("http:");
                        i = i + 4;
                    }
                }
                if (chars[i] == '/') {
                    if (chars[i + 1] == '/') {
                        isUrl = true;
                        stringBuffer = new StringBuffer("//");
                        i = i + 1;
                    } else if (chars[i - 1] == '"') {
                        String strs[] = url.split("/");
                        String newUrl = strs[0] + "//" + strs[2];
                        stringBuffer = new StringBuffer(newUrl + "/");
                        isUrl = true;
                    }
                }

            } else {
                if (isEnding(chars[i])) {
                    String str = stringBuffer.toString();
                    str = compleHttpUrlString(str, url);
                    if (str.length() > 10) {
                        if (!urls.containsKey(str)) {
                            queue.add(str);
                        }
                    }

                    isUrl = false;
                } else {
                    stringBuffer.append(chars[i]);
                }
            }
        }

    }


    public static boolean isEnding(char c) {
        boolean k = false;
        switch (c) {
            case '"':
                k = true;
                break;
            case ' ':
                k = true;
                break;
            case '<':
                k = true;
                break;
            case '\'':
                k = true;
                break;
            case '\n':
                k = true;
                break;
            case '>':
                k = true;
                break;
            case ';':
                k = true;
                break;
            case ')':
                k = true;
                break;
            case '(':
                k = true;
                break;
            default:
                k = false;
                break;
        }
        return k;
    }

}
