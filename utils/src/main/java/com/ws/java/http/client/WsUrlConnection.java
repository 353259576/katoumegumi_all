package com.ws.java.http.client;



import com.ws.java.http.client.model.HttpRequestBody;
import com.ws.java.http.client.model.HttpResponseBody;
import com.ws.java.http.client.model.WsRequestProperty;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WsUrlConnection {
    private static char hrefList[] = new char[]{'h', 'r', 'e', 'f', '=', '"'};
    private static char htmlList[] = new char[]{'"', 'h', 't', 't', 'p'};
    private static char htmlList1[] = new char[]{'\'', 'h', 't', 't', 'p'};
    private static char wwwList[] = new char[]{'w', 'w', 'w'};
    private static String HTMLTYPE = "text/html";
    private static String IMAGETYPE = "image";
    private static String JSTYPE = "application/x-javascript";
    private static String JSONTYPE = "application/json";
    //private static char dianList[] = new char[]{'"','/'};
    private static List<char[]> list = new ArrayList<>();

    private static volatile String biaoshi = null;


    public static void main(String[] args) {
       /* HttpRequestBody httpRequestBody = HttpRequestBody.createHttpRequestBody();
        httpRequestBody.addHttpRequestBodyEntry("name", "你好");
        httpRequestBody.addHttpRequestBodyEntry("name", "你好");
        httpRequestBody.addHttpRequestBodyEntry("name", "你好");
        httpRequestBody.addHttpRequestBodyEntry("name", "你好");
        httpRequestBody.addHttpRequestBodyEntry("name", "你好");
        httpRequestBody.addHttpRequestBodyEntry("name", "你好");
        // File file = new File("D:\\下载\\inst.exe");
        // httpRequestBody.addHttpRequestBodyEntry("file1",file);
        //String str = fileMapToString(httpRequestBody);
        //System.out.println(str);

        HttpResponseBody httpResponseBody = urlLink("https://www.bilibili.com/", "GET", null);
        System.out.println(httpResponseBody.getCode());
        //System.out.println(httpResponseBody.getResponseBodyToString());
        System.out.println(httpResponseBody.getErrorResponseBodyToString());
        System.out.println(JSON.toJSONString(httpResponseBody.getHeaders()));*/

       try {
           System.setProperty("http.proxyHost", "localhost");
           System.setProperty("https.proxyHost", "127.0.0.1");
           System.setProperty("http.proxyPort", "8888");
           System.setProperty("https.proxyPort", "8888");
           HttpResponseBody httpResponseBody = WsUrlConnection.urlLink(HttpRequestBody.createHttpRequestBody().setUrl("https://www.bilibili.com").setMethod("GET").setGZIP(true));
           System.out.println(httpResponseBody);
           /*TimerEnity timerEnity = new TimerEnity();
           timerEnity.setInitiatorUrl("订单回调");
           timerEnity.setAddition("fsdfsdf"+"-"+1);
           timerEnity.setCallbackDate(new Date(System.currentTimeMillis()+ 1000l*60l));
           timerEnity.setCallbackUrl("http://localhost:1996/order/timer/callback");
           HttpRequestBody httpRequestBody = HttpRequestBody.createHttpRequestBody(timerEnity);
           httpRequestBody.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
           //httpRequestBody.setStringHttpRequestBody(JSON.toJSONString(timerEnity));
           HttpResponseBody responseBody = WsUrlConnection.urlLink("http://localhost:2011/timer/addTimer","POST",httpRequestBody);
           System.out.println(responseBody.getResponseBodyToString());*/
       }catch (Exception e){
           e.printStackTrace();
       }

    }


    public static String getPath() {
        String str = WsUrlConnection.class.getResource("/").getPath();
        try {
            return URLDecoder.decode(str, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
            return str;
        }
    }

    public static HttpResponseBody urlLink(String urlpath, HttpRequestBody httpRequestBody) {
        //HttpRequestBody httpRequestBody = HttpRequestBody.createHttpRequestBody(dataObject);
        return urlLink(urlpath, httpRequestBody, null, null);
    }


    public static HttpResponseBody urlLink(HttpRequestBody httpRequestBody){
        if(httpRequestBody.isHttps()){
            return httpsRequest(httpRequestBody);
        }else {
            return httpRequest(httpRequestBody);
        }
    }

    public static HttpResponseBody urlLink(String urlpath, HttpRequestBody httpRequestBody, String pkcsPath, String pkcsPassword){
        if(httpRequestBody == null){
            httpRequestBody = HttpRequestBody.createHttpRequestBody();
        }
        httpRequestBody.setUrl(urlpath);
        httpRequestBody.setPkcsPath(pkcsPath);
        httpRequestBody.setPkcsPassword(pkcsPassword);
        return urlLink(httpRequestBody);
    }


    public static HttpResponseBody httpsRequest(HttpRequestBody httpRequestBody){
        InputStream inputStream = null;
        InputStream eInputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        OutputStream outputStream = null;
        Integer resultCode = null;
        String textType = null;
        FileInputStream fileInputStream = null;
        if(httpRequestBody == null){
            httpRequestBody = HttpRequestBody.createHttpRequestBody();
        }
        HttpResponseBody httpResponseBody = HttpResponseBody.createHttpResponseBody();
        String urlpath = httpRequestBody.getUrl();
        try {
            URL url = new URL(urlpath);
            URLConnection urlConnection = url.openConnection();
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) urlConnection;
            //SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            TrustManager trustManager[] = {new X509TrustManager() {
                //检查本地证书
                @Override
                public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    httpResponseBody.setCilentCertificateName(s);
                    httpResponseBody.setCilentX509Certificates(x509Certificates);
					/*System.out.println("本地证书名称："+s);
					if(x509Certificates != null){
						System.out.println("本地证书内容为" + JSON.toJSONString(x509Certificates));
					}*/
                }

                //检查服务端证书
                @Override
                public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    httpResponseBody.setServerCertificateName(s);
                    httpResponseBody.setServerX509Certificates(x509Certificates);
					/*System.out.println("服务器证书名称："+s);
					if(x509Certificates != null){
						System.out.println("服务器证书内容为" + JSON.toJSONString(x509Certificates));
					}*/
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    //System.out.println("调用了本函数");
                    return new X509Certificate[0];
                }
            }};
            if (httpRequestBody.getPkcsPath() == null) {
                sslContext.init(null, trustManager, new SecureRandom());
            } else {
                File file = new File(httpRequestBody.getPkcsPath());
                KeyStore keyStore = KeyStore.getInstance("PKCS12");
                fileInputStream = new FileInputStream(file);
                keyStore.load(fileInputStream, httpRequestBody.getPkcsPassword().toCharArray());
                KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                keyManagerFactory.init(keyStore, httpRequestBody.getPkcsPassword().toCharArray());
                sslContext.init(keyManagerFactory.getKeyManagers(), trustManager, new SecureRandom());
            }
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            httpsURLConnection.setSSLSocketFactory(sslSocketFactory);
            byte data[] = httpRequestBody.getbyteHttpRequestBody();
            if (data != null&&data.length > 0) {
                httpsURLConnection.setDoInput(true);
            }
            httpsURLConnection.setDoOutput(true);
            httpsURLConnection.setRequestMethod(httpRequestBody.getMethod());

            if (!httpRequestBody.getRequestProperty().isEmpty()) {
                List<WsRequestProperty> wsRequestProperties = httpRequestBody.getRequestProperty();
                for (WsRequestProperty wsRequestProperty:wsRequestProperties) {
                    httpsURLConnection.addRequestProperty(wsRequestProperty.getKey(), wsRequestProperty.getValue());
                }
            }

            httpsURLConnection.connect();

            if("POST".equals(httpRequestBody.getMethod())){
                if (data != null&&data.length > 0) {
                    outputStream = httpsURLConnection.getOutputStream();
                    bufferedOutputStream = new BufferedOutputStream(outputStream);
                    bufferedOutputStream.write(data);
                    bufferedOutputStream.flush();
                    bufferedOutputStream.close();
                    data = null;
                }
            }
            resultCode = httpsURLConnection.getResponseCode();
            httpResponseBody.setCode(resultCode);
            Map<String, List<String>> map = httpsURLConnection.getHeaderFields();
            httpResponseBody.setHeaders(map);
            try {
                inputStream = httpsURLConnection.getInputStream();
            }catch (Exception e){
                //e.printStackTrace();
            }
            try {
                eInputStream = httpsURLConnection.getErrorStream();
            }catch (Exception e){
                //e.printStackTrace();
            }





            //textType = httpsURLConnection.getRequestProperty("Content-Type").split("=")[1];

            if(inputStream != null){
                httpResponseBody.setReturnBytes(WsUrlConnection.readInputsteam(inputStream));
            }
            if(eInputStream != null){
                httpResponseBody.setErrorReturnBytes(WsUrlConnection.readInputsteam(eInputStream));
            }
            httpResponseBody.build();
            return httpResponseBody;


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeSteam(outputStream);
            closeSteam(bufferedOutputStream);
            closeSteam(inputStream);
            closeSteam(eInputStream);
            closeSteam(fileInputStream);
        }
        return null;
    }

    public static HttpResponseBody httpRequest(HttpRequestBody httpRequestBody) {

        InputStream inputStream = null;
        InputStream eInputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        OutputStream outputStream = null;
        Integer resultCode = null;
        String textType = null;
        if (httpRequestBody == null) {
            httpRequestBody = HttpRequestBody.createHttpRequestBody();
        }
        HttpResponseBody httpResponseBody = HttpResponseBody.createHttpResponseBody();
        try {
            URL url = new URL(httpRequestBody.getUrl());
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            byte data[] = httpRequestBody.getbyteHttpRequestBody();
            if (data != null && data.length > 0) {
                httpURLConnection.setDoInput(true);
            }
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestMethod(httpRequestBody.getMethod());
            //data = httpRequestBody.getHttpRequestBodyString();
            if (!httpRequestBody.getRequestProperty().isEmpty()) {
                List<WsRequestProperty> wsRequestProperties = httpRequestBody.getRequestProperty();
                for (WsRequestProperty wsRequestProperty : wsRequestProperties) {
                    httpURLConnection.addRequestProperty(wsRequestProperty.getKey(), wsRequestProperty.getValue());
                }
            }
            httpURLConnection.connect();
            if ("POST".equals(httpRequestBody.getMethod())) {
                if (data != null && data.length > 0) {
                    outputStream = httpURLConnection.getOutputStream();
                    bufferedOutputStream = new BufferedOutputStream(outputStream);
                    bufferedOutputStream.write(data);
                    bufferedOutputStream.flush();
                    bufferedOutputStream.close();
                    data = null;
                }
            }
            resultCode = httpURLConnection.getResponseCode();
            httpResponseBody.setCode(resultCode);
            Map<String, List<String>> map = httpURLConnection.getHeaderFields();
            httpResponseBody.setHeaders(map);

            try {
                inputStream = httpURLConnection.getInputStream();
            } catch (Exception e) {
                //e.printStackTrace();
            }
            try {
                eInputStream = httpURLConnection.getErrorStream();
            } catch (Exception e) {
                //e.printStackTrace();
            }


            if (inputStream != null) {
                httpResponseBody.setReturnBytes(WsUrlConnection.readInputsteam(inputStream));
            }
            if (eInputStream != null) {
                httpResponseBody.setErrorReturnBytes(WsUrlConnection.readInputsteam(eInputStream));
            }
            httpResponseBody.build();
            return httpResponseBody;


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeSteam(outputStream);
            closeSteam(bufferedOutputStream);
            closeSteam(inputStream);
            closeSteam(eInputStream);
        }
        return null;

    }



    public static byte[] readInputsteam(InputStream inputStream){
        WritableByteChannel writableByteChannel = null;
        ReadableByteChannel readableByteChannel = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        ByteBuffer byteBuffer = null;
        byte bytes[] = null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            readableByteChannel = Channels.newChannel(inputStream);
            writableByteChannel = Channels.newChannel(byteArrayOutputStream);
            byteBuffer = ByteBuffer.allocate(1024);
            while (readableByteChannel.read(byteBuffer) != -1){
                byteBuffer.flip();
                while (byteBuffer.hasRemaining()){
                    writableByteChannel.write(byteBuffer);
                }
                byteBuffer.clear();
            }
            bytes = byteArrayOutputStream.toByteArray();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if(writableByteChannel != null) {
                    writableByteChannel.close();
                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                writableByteChannel = null;
            }
            try {
                if(readableByteChannel != null) {
                    readableByteChannel.close();
                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                readableByteChannel = null;
            }
            try {
                if(byteArrayOutputStream != null){
                    byteArrayOutputStream.flush();
                    byteArrayOutputStream.close();
                }

            }catch (Exception e){
                e.printStackTrace();
            }finally {
                byteArrayOutputStream = null;
            }
            try {
                if(inputStream != null) {
                    inputStream.close();
                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                inputStream = null;
            }
        }
        return bytes;
    }

    public static OutputStream writeOutputsteam(InputStream inputStream,OutputStream outputStream){
        WritableByteChannel writableByteChannel = null;
        ReadableByteChannel readableByteChannel = null;
        ByteBuffer byteBuffer = null;
        try {
            readableByteChannel = Channels.newChannel(inputStream);
            writableByteChannel = Channels.newChannel(outputStream);
            byteBuffer = ByteBuffer.allocate(1024);
            while (readableByteChannel.read(byteBuffer) != -1){
                byteBuffer.flip();
                while (byteBuffer.hasRemaining()){
                    writableByteChannel.write(byteBuffer);
                }
                byteBuffer.clear();
            }

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            closeSteam(writableByteChannel);
            closeSteam(readableByteChannel);
            closeSteam(inputStream);
        }
        return outputStream;
    }


    public static void closeSteam(Closeable closeable){
        if(closeable != null) {
            try {
                if (closeable instanceof Flushable) {
                    ((Flushable) closeable).flush();
                }
                closeable.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
