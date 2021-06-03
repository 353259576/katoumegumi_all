package cn.katoumegumi.java.http.client.model;


import cn.katoumegumi.java.common.WsFieldUtils;
import cn.katoumegumi.java.common.WsListUtils;
import cn.katoumegumi.java.common.WsStringUtils;
import cn.katoumegumi.java.http.client.WsNettyClient;
import cn.katoumegumi.java.http.model.BaseEntity;
import cn.katoumegumi.java.http.model.FileEntity;
import cn.katoumegumi.java.http.model.ValueEntity;
import cn.katoumegumi.java.http.utils.HttpUtils;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.*;
import java.util.zip.GZIPOutputStream;

public class HttpRequestBody {
    //private volatile List<HttpRequestBodyEntry> httpRequestBodyEntries = new ArrayList<>();

    private final List<BaseEntity> bodyEntityList = new ArrayList<>();
    private final List<WsRequestProperty> requestProperty = new ArrayList<>();
    private String stringHttpRequestBody = null;
    private String charset = "UTF-8";
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
    private int retryNumber = 3;

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
            Field[] fields = WsFieldUtils.getFieldAll(object.getClass());
            String value = null;
            for (int i = 0; i < fields.length; i++) {
                value = WsStringUtils.anyToString(WsFieldUtils.getValue(object, fields[i]));
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


    /*public HttpRequestBody addHttpRequestBodyEntry(HttpRequestBodyEntry httpRequestBodyEntry) {
        httpRequestBodyEntries.add(httpRequestBodyEntry);
        if (httpRequestBodyEntry.getInputStreamValue() != null) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        } else if (httpRequestBodyEntry.getObjectValue() != null) {
            mediaType = MediaType.APPLICATION_JSON_VALUE;
        }
        return this;
    }*/

    public HttpRequestBody addHttpRequestBodyEntry(Map<String, Object> map) {
        /*if (!(map == null || map.isEmpty())) {
            Set<Map.Entry<String, Object>> set = map.entrySet();
            for (Map.Entry<String, Object> e : set) {
                httpRequestBodyEntries.add(new HttpRequestBodyEntry(e.getKey(), WsStringUtils.anyToString(e.getValue())));
            }
            this.mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE;
        }*/
        if (!(map == null || map.isEmpty())) {
            Set<Map.Entry<String, Object>> set = map.entrySet();
            for (Map.Entry<String, Object> e : set) {
                bodyEntityList.add(new ValueEntity().setValue(e.getValue()).setName(e.getKey()));
            }
            this.mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE;
        }
        return this;
    }


    public HttpRequestBody addHttpRequestBodyEntry(String name, String value) {
        //httpRequestBodyEntries.add(new HttpRequestBodyEntry(name, value));
        bodyEntityList.add(new ValueEntity().setValue(value).setName(name));
        this.mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE;
        return this;
    }

    public HttpRequestBody addHttpRequestBodyEntry(String name, File file) {
        //httpRequestBodyEntries.add(new HttpRequestBodyEntry(name, file));
        bodyEntityList.add(new FileEntity().setValue(file).setName(name));
        this.mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        return this;
    }

    public HttpRequestBody addHttpRequestBodyEntry(String name, String fileName, InputStream inputStream) {
        //httpRequestBodyEntries.add(new HttpRequestBodyEntry(name, fileName, inputStream));
        bodyEntityList.add(new FileEntity().setValue(inputStream).setFileName(fileName).setName(name));
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

    /*public List<HttpRequestBodyEntry> getHttpRequestBodyEntries() {
        return this.httpRequestBodyEntries;
    }*/

    public byte[] getByteHttpRequestBody() {
        byte[] bytes = null;
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
                bytes = this.stringHttpRequestBody.getBytes(charset);
            } catch (Exception e) {
                e.printStackTrace();
                bytes = this.stringHttpRequestBody.getBytes();
            }
        }
        if (isGZIP) {
            bytes = compressByGZIP(bytes);
        }
        return bytes;
    }

    public String getStringHttpRequestBody() {
        try {
            return new String(getByteHttpRequestBody(), charset);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
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


    //生成formData数据格式
    public byte[] multipartFormData(HttpRequestBody httpRequestBody) throws RuntimeException {
        String boundary = HttpUtils.getFormDataBoundary();
        httpRequestBody.setRequestProperty("content-type", "multipart/form-data; boundary=" + boundary);
        return HttpUtils.toFormData(bodyEntityList, boundary, charset);
    }


    //生成默认数据格式
    public byte[] simpleFormData(HttpRequestBody httpRequestBody) {
        return HttpUtils.toBaseForm(bodyEntityList, charset);
    }

    //生成JSON数据格式
    public byte[] jsonFormData(HttpRequestBody httpRequestBody) {
        httpRequestBody.setRequestProperty("content-type", MediaType.APPLICATION_JSON_VALUE.getCoed());
        Map<String, Object> map = new HashMap<>();
        for (BaseEntity entity : bodyEntityList) {
            if (entity instanceof ValueEntity) {
                map.put(entity.getName(), ((ValueEntity) entity).getValue());
            }
        }
        if (WsListUtils.isNotEmpty(map)) {

            //byte[] bytes = JSON.toJSONBytes(map);
            byte[] bytes = new byte[0];
            try {
                bytes = new Gson().toJson(map).getBytes(charset);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return bytes;
        }
        return null;
    }

    //生成xml数据格式
    public byte[] xmlFormData(List<HttpRequestBodyEntry> list) {
        return null;
    }


    public byte[] compressByGZIP(byte[] bytes) {
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
            byte[] newBytes = byteArrayOutputStream.toByteArray();
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
            byte[] bytes = getByteHttpRequestBody();
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
            String[] strs = url.split("/");
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

    public String getCharset() {
        return this.charset;
    }

    public HttpRequestBody setCharset(String charset) {
        this.charset = charset;
        return this;
    }

    public String getMethod() {
        return method;
    }

    public HttpRequestBody setMethod(String method) {
        this.method = method;
        return this;
    }

    public HttpRequestBody setUserAgent(String userAgent) {
        setRequestProperty("User-Agent", userAgent);
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
        if (this.port == 0) {
            this.port = this.uri.getPort();
        }
        if (this.port == 0) {
            if (this.isHttps) {
                this.port = 80;
            } else {
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

    public int getRetryNumber() {
        return this.retryNumber;
    }

    public HttpRequestBody setRetryNumber(int retryNumber) {
        this.retryNumber = retryNumber;
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
            bodyEntityList.sort((o1, o2) -> -(o1.compareTo(o2)));
        } else if ("asc".equals(sort)) {
            bodyEntityList.sort((o1, o2) -> o1.compareTo(o2));
        }
        /*if ("desc".equals(sort)) {
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
        }*/

        return this;
    }
}
