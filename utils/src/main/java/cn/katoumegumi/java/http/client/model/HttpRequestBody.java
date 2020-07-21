package cn.katoumegumi.java.http.client.model;


import cn.katoumegumi.java.common.WsFieldUtils;
import cn.katoumegumi.java.common.WsStringUtils;
import cn.katoumegumi.java.http.client.WsNettyClient;
import com.alibaba.fastjson.JSON;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.*;
import java.util.zip.GZIPOutputStream;

public class HttpRequestBody {
    private static final char[] MULTIPART_CHARS = "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private volatile List<HttpRequestBodyEntry> httpRequestBodyEntries = new ArrayList<>();
    private String stringHttpRequestBody = null;
    private volatile List<WsRequestProperty> requestProperty = new ArrayList<>();
    private String charest = "UTF-8";
    private String method = "GET";
    private boolean isHttps = false;
    private String contextType;
    private String url;
    private String pkcsPath;
    private String pkcsPassword;
    private MediaType mediaType = MediaType.ALL_VALUE;
    private boolean isGZIP = false;
    private URI uri;
    private int port;
    private boolean useChunked = false;
    private long expirationTime = 60L * 1000L;
    private int retryNuumber = 3;

    private HttpRequestBody() {

    }

    public static HttpRequestBody createHttpRequestBody() {
        return new HttpRequestBody();
    }

    public static HttpRequestBody createHttpRequestBody(Object object) {
        HttpRequestBody httpRequestBody = new HttpRequestBody();
        if (object == null) {
            return httpRequestBody;
        }
        if (object instanceof Map) {
            Map map = (Map) object;
            Set<Map.Entry> entries = map.entrySet();
            Iterator<Map.Entry> iterator = entries.iterator();
            Map.Entry entry = null;
            String key;
            String value;
            while (iterator.hasNext()) {
                entry = iterator.next();
                key = WsStringUtils.anyToString(entry.getKey());
                if (key == null) {
                    continue;
                }
                value = WsStringUtils.anyToString(entry.getValue());
                if (value == null) {
                    continue;
                }
                httpRequestBody.addHttpRequestBodyEntry(key, value);
            }
            return httpRequestBody;
        } else if (object instanceof String) {
            httpRequestBody.setStringHttpRequestBody((String) object);
        } else {
            Field fields[] = WsFieldUtils.getFieldAll(object.getClass());
            String value = null;
            for (int i = 0; i < fields.length; i++) {
                value = WsStringUtils.anyToString(WsFieldUtils.getFieldValueForName(fields[i], object));
                if (value != null) {
                    httpRequestBody.addHttpRequestBodyEntry(fields[i].getName(), value);
                }
            }

        }
        return httpRequestBody;
    }

    public HttpResponseTask nettyBuild() {
        return WsNettyClient.clientStart(this);
    }

   /* public HttpResponseBody build(){
        return WsUrlConnection.urlLink(this);
    }*/


    public HttpRequestBody addHttpRequestBodyEntry(HttpRequestBodyEntry httpRequestBodyEntry) {
        httpRequestBodyEntries.add(httpRequestBodyEntry);
        if (httpRequestBodyEntry.getInputStreamValue() != null) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        } else if (httpRequestBodyEntry.getObjectValue() != null) {
            mediaType = MediaType.APPLICATION_JSON_VALUE;
        }
        return this;
    }

    public HttpRequestBody addHttpRequestBodyEntry(Map<String, Object> map) {
        if (!(map == null || map.isEmpty())) {
            Set<Map.Entry<String, Object>> set = map.entrySet();
            for (Map.Entry<String, Object> e : set) {
                httpRequestBodyEntries.add(new HttpRequestBodyEntry(e.getKey(), WsStringUtils.anyToString(e.getValue())));
            }
            this.mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE;
        }
        return this;
    }


    public HttpRequestBody addHttpRequestBodyEntry(String name, String value) {
        httpRequestBodyEntries.add(new HttpRequestBodyEntry(name, value));
        this.mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE;
        return this;
    }

    public HttpRequestBody addHttpRequestBodyEntry(String name, File file) {
        httpRequestBodyEntries.add(new HttpRequestBodyEntry(name, file));
        this.mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        return this;
    }

    public HttpRequestBody addHttpRequestBodyEntry(String name, String fileName, InputStream inputStream) {
        httpRequestBodyEntries.add(new HttpRequestBodyEntry(name, fileName, inputStream));
        this.mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        return this;
    }


    public synchronized HttpRequestBody setRequestProperty(String key, String value) {

        boolean k = true;
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < requestProperty.size(); i++) {
            if (requestProperty.get(i).getKey().equals(key)) {
                list.add(i);
            }
        }
        if (!list.isEmpty()) {
            for (Integer i : list) {
                requestProperty.remove(i.intValue());
            }
        }
        requestProperty.add(WsRequestProperty.createWsRequestProPerty(key, value));

        return this;
    }

    public HttpRequestBody addRequestProperty(String key, String value) {
        requestProperty.add(WsRequestProperty.createWsRequestProPerty(key, value));
        return this;
    }

    public List<WsRequestProperty> getRequestProperty() {
        return requestProperty;
    }

    public List<HttpRequestBodyEntry> getHttpRequestBodyEntries() {
        return this.httpRequestBodyEntries;
    }

    public byte[] getbyteHttpRequestBody() {
        byte bytes[] = null;
        if (WsStringUtils.isBlank(this.stringHttpRequestBody)) {
            switch (mediaType) {
                case APPLICATION_FORM_URLENCODED_VALUE:
                    bytes = simpleFormData(this);
                    break;
                case APPLICATION_JSON_VALUE:
                    bytes = jsonFormData(this);
                    break;
                case APPLICATION_OCTET_STREAM_VALUE:
                    bytes = multipartFormData(this);
                    break;
                default:
                    bytes = simpleFormData(this);
                    break;
            }
        } else {
            try {

                bytes = this.stringHttpRequestBody.getBytes(charest);
            } catch (Exception e) {
                e.printStackTrace();
                bytes = this.stringHttpRequestBody.getBytes();
            }
        }
        if (isGZIP) {
            bytes = cmpressByGZIP(bytes);
        }
        return bytes;
    }

    public String getStringHttpRequestBody() {
        return new String(getbyteHttpRequestBody());
    }

    public HttpRequestBody setStringHttpRequestBody(String stringHttpRequestBody) {
        this.stringHttpRequestBody = stringHttpRequestBody;
        if (stringHttpRequestBody.startsWith("{")) {
            mediaType = MediaType.APPLICATION_JSON_VALUE;
        } else if (stringHttpRequestBody.startsWith("<")) {
            mediaType = MediaType.APPLICATION_XML_VALUE;
        }
        return this;
    }






 /*   public byte[] simpleFormData(HttpRequestBody httpRequestBody) throws RuntimeException {
        StringBuffer stringBuffer = new StringBuffer();
        if (httpRequestBody == null) {
            return null;
        }
        if (isJson) {
            Map map = new LinkedHashMap();
            for (HttpRequestBodyEntry httpRequestBodyEntry : httpRequestBody.getHttpRequestBodyEntries()) {
                if (!WsStringUtils.isBlank(httpRequestBodyEntry.getValue())) {
                    map.put(httpRequestBodyEntry.getName(), httpRequestBodyEntry.getValue());
                } else if (httpRequestBodyEntry.getObjectValue() != null) {
                    map.put(httpRequestBodyEntry.getName(), httpRequestBodyEntry.getObjectValue());
                }
            }
            String str = JSON.toJSONString(map);
            httpRequestBody.setStringHttpRequestBody(str);
            if (str.length() > 0) {
                try {
                    //return URLEncoder.encode(stringBuffer.toString(),charest).getBytes(charest);
                    return str.getBytes(charest);
                } catch (Exception e) {
                    e.printStackTrace();
                    return str.getBytes();
                }

            }

        } else {
            for (HttpRequestBodyEntry httpRequestBodyEntry : httpRequestBody.getHttpRequestBodyEntries()) {
                if (!httpRequestBodyEntry.isFile()) {
                    stringBuffer.append('&');
                    stringBuffer.append(httpRequestBodyEntry.getName());
                    stringBuffer.append('=');
                    stringBuffer.append(httpRequestBodyEntry.getValue());
                }
            }
            if (stringBuffer.length() > 0) {
                stringBuffer.delete(0, 1);
                try {
                    //return URLEncoder.encode(stringBuffer.toString(),charest).getBytes(charest);
                    return stringBuffer.toString().getBytes(charest);
                } catch (Exception e) {
                    e.printStackTrace();
                    return stringBuffer.toString().getBytes();
                }

            }

        }
        return null;
    }*/

    //生成文件数据格式
    public byte[] multipartFormData(HttpRequestBody httpRequestBody) throws RuntimeException {
        Random random = new Random();
        int j;
        String getLine = "\r\n";
        String fileType = "Content-Type: application/octet-stream";
        String doubleBar = "--";
        String biaoshi = "----WebKitFormBoundary";
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 16; i++) {
            j = random.nextInt(MULTIPART_CHARS.length - 2) + 2;
            sb.append(MULTIPART_CHARS[j]);
        }
        biaoshi = biaoshi + sb.toString();
        httpRequestBody.setRequestProperty("content-type", "multipart/form-data; boundary=" + biaoshi);
        StringBuffer stringBuffer = new StringBuffer();

        List<HttpRequestBodyEntry> httpRequestBodyEntries = httpRequestBody.getHttpRequestBodyEntries();
        if (httpRequestBodyEntries.isEmpty()) {
            return null;
        }
        HttpRequestBodyEntry httpRequestBodyEntry = null;
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);


        ByteArrayOutputStream byteArrayOutputStream = null;
        WritableByteChannel writableByteChannel = null;


        byte[] bytes;
        int size;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            writableByteChannel = Channels.newChannel(byteArrayOutputStream);
            for (int i = 0; i < httpRequestBodyEntries.size(); i++) {
                httpRequestBodyEntry = httpRequestBodyEntries.get(i);
                if (httpRequestBodyEntry.isFile()) {
                    InputStream inputStream = null;
                    ReadableByteChannel readableByteChannel = null;
                    try {
                        String name = "Content-Disposition: form-data; name=\"" + httpRequestBodyEntry.getName() + "\"; filename=\"" + httpRequestBodyEntry.getValue() + "\"";
                        stringBuffer.append(doubleBar + biaoshi);
                        stringBuffer.append(getLine);
                        stringBuffer.append(name);
                        stringBuffer.append(getLine);
                        stringBuffer.append(fileType);
                        stringBuffer.append(getLine);
                        stringBuffer.append(getLine);
                        //File f = entity.getValue();
                        //FileInputStream fileInputStream = new FileInputStream(f);
                        try {
                            bytes = stringBuffer.toString().getBytes(httpRequestBody.getCharest());
                        } catch (Exception e) {
                            e.printStackTrace();
                            bytes = stringBuffer.toString().getBytes();
                        }
                        bytes = stringBuffer.toString().getBytes();
                        size = bytes.length;
                        for (int k = 0; k < size / 1024 + 1; k++) {
                            byteBuffer.put(bytes, k * 1024, (k + 1) * 1024 > size ? size : (k + 1) * 1024);
                            byteBuffer.flip();
                            while (byteBuffer.hasRemaining()) {
                                writableByteChannel.write(byteBuffer);
                            }
                            byteBuffer.clear();
                        }
                        stringBuffer = new StringBuffer();


                        inputStream = httpRequestBodyEntry.getInputStreamValue();
                        readableByteChannel = Channels.newChannel(inputStream);

                        while (readableByteChannel.read(byteBuffer) != -1) {
                            byteBuffer.flip();
                            while (byteBuffer.hasRemaining()) {
                                writableByteChannel.write(byteBuffer);
                            }
                            byteBuffer.clear();
                        }


                    /*byte[] by = byteArrayOutputStream.toByteArray();
                    for (int k = 0; k < by.length; k++) {
                        stringBuffer.append(by[k]);
                    }*/
                        //stringBuffer.append(getLine);
                        try {
                            byteBuffer.put(getLine.getBytes(httpRequestBody.getCharest()));
                        } catch (Exception e) {
                            e.printStackTrace();
                            byteBuffer.put(getLine.getBytes());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (readableByteChannel != null) {
                            try {
                                readableByteChannel.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                readableByteChannel = null;
                            }
                        }


                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                inputStream = null;
                            }
                        }
                    }

                } else {
                    String name = "Content-Disposition: form-data; name=\"" + httpRequestBodyEntry.getName() + "\"";
                    stringBuffer.append(doubleBar + biaoshi);
                    stringBuffer.append(getLine);
                    stringBuffer.append(name);
                    stringBuffer.append(getLine);
                    stringBuffer.append(getLine);
                    stringBuffer.append(httpRequestBodyEntry.getValue());
                    stringBuffer.append(getLine);

                    try {
                        bytes = stringBuffer.toString().getBytes(httpRequestBody.getCharest());
                    } catch (Exception e) {
                        e.printStackTrace();
                        bytes = stringBuffer.toString().getBytes();
                    }


                    size = bytes.length;
                    for (int k = 0; k < size / 1024 + 1; k++) {
                        byteBuffer.put(bytes, k * 1024, (k + 1) * 1024 > size ? size : (k + 1) * 1024);
                        byteBuffer.flip();
                        while (byteBuffer.hasRemaining()) {
                            writableByteChannel.write(byteBuffer);
                        }
                        byteBuffer.clear();
                    }
                    stringBuffer = new StringBuffer();
                }
            }
            stringBuffer.append(doubleBar + biaoshi + doubleBar);
            try {
                bytes = stringBuffer.toString().getBytes(httpRequestBody.getCharest());
            } catch (Exception e) {
                e.printStackTrace();
                bytes = stringBuffer.toString().getBytes();
            }
            size = bytes.length;
            for (int k = 0; k < size / 1024 + 1; k++) {
                byteBuffer.put(bytes, k * 1024, (k + 1) * 1024 > size ? size : (k + 1) * 1024);
                byteBuffer.flip();
                while (byteBuffer.hasRemaining()) {
                    writableByteChannel.write(byteBuffer);
                }
                byteBuffer.clear();
            }
            //return stringBuffer.toString();
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {

                if (writableByteChannel != null) {
                    try {
                        writableByteChannel.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        writableByteChannel = null;
                    }

                }

                if (byteArrayOutputStream != null) {
                    try {
                        byteArrayOutputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        byteArrayOutputStream = null;
                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return null;

    }


    //生成默认数据格式
    public byte[] simpleFormData(HttpRequestBody httpRequestBody) {
        httpRequestBody.setRequestProperty("content-type", mediaType.getCoed());
        List<HttpRequestBodyEntry> list = httpRequestBody.getHttpRequestBodyEntries();
        List<String> stringList = new ArrayList<>();
        for (HttpRequestBodyEntry httpRequestBodyEntry : list) {
            if (httpRequestBodyEntry.getValue() != null) {
                stringList.add(httpRequestBodyEntry.getName() + "=" + httpRequestBodyEntry.getValue());
            }
        }
        if (stringList.size() > 0) {
            String str = WsStringUtils.jointListString(stringList, "&");
            try {
                return str.getBytes(charest);
            } catch (Exception e) {
                e.printStackTrace();
                return str.getBytes();
            }
        }
        return null;
    }

    //生成JSON数据格式
    public byte[] jsonFormData(HttpRequestBody httpRequestBody) {
        httpRequestBody.setRequestProperty("content-type", MediaType.APPLICATION_JSON_VALUE.getCoed());
        List<HttpRequestBodyEntry> list = httpRequestBody.getHttpRequestBodyEntries();
        Map map = new HashMap();
        for (HttpRequestBodyEntry httpRequestBodyEntry : list) {
            if (httpRequestBodyEntry.getObjectValue() != null) {
                map.put(httpRequestBodyEntry.getName(), httpRequestBodyEntry.getObjectValue());
            } else if (httpRequestBodyEntry.getValue() != null) {
                map.put(httpRequestBodyEntry.getName(), httpRequestBodyEntry.getValue());
            }
        }
        if (!map.isEmpty()) {
            byte[] bytes = JSON.toJSONBytes(map);
            return bytes;
        }
        return null;
    }

    //生成xml数据格式
    public byte[] xmlFormData(List<HttpRequestBodyEntry> list) {
        return null;
    }


    public byte[] cmpressByGZIP(byte bytes[]) {
        if (bytes == null) {
            return bytes;
        }
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
            WritableByteChannel writableByteChannel = Channels.newChannel(gzipOutputStream);
            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
            while (byteBuffer.hasRemaining()) {
                writableByteChannel.write(byteBuffer);
            }
            byteBuffer.clear();
            writableByteChannel.close();
            gzipOutputStream.flush();
            gzipOutputStream.close();
            byteArrayOutputStream.flush();
            byteArrayOutputStream.close();
            byte newBytes[] = byteArrayOutputStream.toByteArray();
            return newBytes;
        } catch (Exception e) {
            e.printStackTrace();
            return bytes;
        }
    }


    //************************************************************************************************************
    public String getContextType() {
        return contextType;
    }

    public HttpRequestBody setContextType(String contextType) {
        this.contextType = contextType;
        return this;
    }

    public String getUrl() {
        if ("GET".equals(method)) {
            //byte[] bytes = simpleFormData(this);
            byte bytes[] = getbyteHttpRequestBody();
            if (bytes != null && bytes.length > 0) {
                return url + "?" + new String(bytes);
            } else {
                return url;
            }

        } else {
            return url;
        }

    }

    public HttpRequestBody setUrl(String url) {
        try {
            URI uri = null;
            String strs[] = url.split("/");
            if (strs.length > 2) {
                String newUrl = strs[0] + "//" + strs[2];
                uri = new URI(newUrl);
            } else {
                uri = new URI(url);
            }

            this.uri = uri;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if ("https".equals(uri.getScheme())) {
            isHttps = true;
            if (uri.getPort() == -1) {
                this.port = 443;
            }
        } else {
            isHttps = false;
            if (uri.getPort() == -1) {
                this.port = 80;
            }
        }
        this.url = url;
        return this;
    }

    public String getPkcsPath() {
        return pkcsPath;
    }

    public HttpRequestBody setPkcsPath(String pkcsPath) {
        this.pkcsPath = pkcsPath;
        return this;
    }

    public String getPkcsPassword() {
        return pkcsPassword;
    }

    public HttpRequestBody setPkcsPassword(String pkcsPassword) {
        this.pkcsPassword = pkcsPassword;
        return this;
    }

    public boolean isHttps() {
        return isHttps;
    }

    public HttpRequestBody setHttps(boolean https) {
        isHttps = https;
        return this;
    }

    public String getCharest() {
        return charest;
    }

    public HttpRequestBody setCharest(String charest) {
        this.charest = charest;
        return this;
    }

    public String getMethod() {
        return method;
    }

    public HttpRequestBody setMethod(String method) {
        this.method = method;
        return this;
    }


    //*********************************************************

    public URI getUri() {
        return uri;
    }

    public HttpRequestBody setUri(URI uri) {
        this.uri = uri;
        return this;
    }

    public int getPort() {
        if(this.port == 0){
            this.port = this.uri.getPort();
        }
        if(this.port == 0){
            if(this.isHttps) {
                this.port = 80;
            }else {
                this.port = 443;
            }
        }




        return port;
    }

    public MediaType getMediaType() {
        return this.mediaType;
    }

    public boolean getGZIP() {
        return this.isGZIP;
    }

    public HttpRequestBody setGZIP(boolean GZIP) {
        this.setRequestProperty("Accept-Encoding", "gzip, deflate");
        this.setRequestProperty("Vary", "Accept-Encoding");
        isGZIP = GZIP;
        return this;
    }

    public boolean isUseChunked() {
        return useChunked;
    }

    public HttpRequestBody setUseChunked(boolean useChunked) {
        this.useChunked = useChunked;
        return this;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public HttpRequestBody setExpirationTime(long expirationTime) {
        this.expirationTime = expirationTime;
        return this;
    }

    public int getRetryNuumber() {
        return retryNuumber;
    }

    public HttpRequestBody setRetryNuumber(int retryNuumber) {
        this.retryNuumber = retryNuumber;
        return this;
    }

    public HttpRequestBody setJson(boolean json) {
        mediaType = MediaType.APPLICATION_JSON_VALUE;
        return this;
    }

    public HttpRequestBody setXml(boolean xml) {
        mediaType = MediaType.APPLICATION_XML_VALUE;
        return this;
    }

    public HttpRequestBody sortBody(String sort) {
        if ("desc".equals(sort)) {
            httpRequestBodyEntries.sort(new Comparator<HttpRequestBodyEntry>() {
                @Override
                public int compare(HttpRequestBodyEntry o1, HttpRequestBodyEntry o2) {
                    return -(o1.compareTo(o2));
                }
            });
        } else if ("asc".equals(sort)) {
            httpRequestBodyEntries.sort(new Comparator<HttpRequestBodyEntry>() {
                @Override
                public int compare(HttpRequestBodyEntry o1, HttpRequestBodyEntry o2) {
                    return o1.compareTo(o2);
                }
            });
        }

        return this;
    }
}
