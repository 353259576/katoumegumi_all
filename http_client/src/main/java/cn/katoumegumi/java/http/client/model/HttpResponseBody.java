package cn.katoumegumi.java.http.client.model;

import cn.katoumegumi.java.common.WsStringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class HttpResponseBody {
    private String url;
    private int code;
    private String httpVersion;
    private boolean isKeepAlive;
    private String contentType;
    private String charSet;
    private byte[] returnBytes;
    private byte[] errorReturnBytes;
    private Map<String, List<String>> headers;

    private String clientCertificateName;
    private X509Certificate[] clientX509Certificates;
    private String serverCertificateName;
    private X509Certificate[] serverX509Certificates;

    private boolean isGzip = false;

    private HttpResponseBody() {
        this.headers = new HashMap<>();
    }

    private HttpResponseBody(int code, byte[] returnBytes, Map<String, List<String>> headers) {
        this.code = code;
        this.returnBytes = returnBytes;
        this.headers = headers;
    }

    public static HttpResponseBody createHttpResponseBody() {
        return new HttpResponseBody();
    }

    public static HttpResponseBody createHttpResponseBody(int code, byte[] returnBytes, Map<String, List<String>> headers) {
        return new HttpResponseBody(code, returnBytes, headers);
    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<String> getHeaderProperty(String key) {
        return headers.get(key);
    }


    public void setHeaderProperty(String key, List<String> value) {
        headers.put(key, value);
    }

    public synchronized void setHeaderProperty(String key, String value) {
        List<String> list = headers.get(key);
        if (list == null) {
            list = new ArrayList<>(Arrays.asList(value.split(",")));
            headers.put(key, list);
        } else {
            list.addAll(Arrays.asList(value.split(",")));
        }
    }

    public String getResponseBodyToString(String charset) {

        try {
            if (returnBytes == null) {
                return new String(errorReturnBytes, charset);
            } else {
                return new String(returnBytes, charset);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getResponseBodyToString() {
        if (WsStringUtils.isNotBlank(charSet)) {
            return getResponseBodyToString(charSet);
        } else {
            return getResponseBodyToString("UTF-8");
        }

    }

    public String getErrorResponseBodyToString(String charset) {
        try {
            if (errorReturnBytes == null) {
                return null;
            }
            return new String(errorReturnBytes, charset);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getErrorResponseBodyToString() {
        if (charSet != null) {
            return getErrorResponseBodyToString(charSet);
        } else {
            return getResponseBodyToString("UTF-8");
        }

    }


    public void build() {
        List<String> list = headers.get("Connection");
        this.isKeepAlive = false;
        if (!(list == null || list.size() == 0)) {
            if ("keep-alive".equals(list.get(0))) {
                this.isKeepAlive = true;
            }
        }
        list = headers.get(null);
        if (!(list == null || list.size() == 0)) {
            int k = list.get(0).indexOf(' ');
            if (k < 1) {
                k = list.get(0).length();
            }
            httpVersion = list.get(0).substring(0, k);
        }

        list = headers.get("Content-Type");
        if (!(list == null || list.size() == 0)) {
            int l = list.get(0).length();
            int k = list.get(0).toLowerCase().indexOf("charset");
            if (k > 1) {
                contentType = list.get(0).substring(0, k);
                if (l - k > 10) {
                    charSet = list.get(0).substring(k + 8, l);
                }
            } else {
                if (list.get(0).startsWith("charset")) {
                    charSet = list.get(0).substring(list.get(0).indexOf("charset") + 8, l);
                } else {
                    contentType = list.get(0);
                }
            }
        }
        list = headers.get("Content-Encoding");
        if (!(list == null || list.isEmpty())) {
            for (Object o : list) {
                String str = (String) o;
                if ("gzip".equals(str.toLowerCase())) {
                    isGzip = true;
                    if (!(returnBytes == null || returnBytes.length == 0)) {
                        this.returnBytes = unCompressByGZIP(this.returnBytes);
                    }
                    if (!(errorReturnBytes == null || errorReturnBytes.length == 0)) {
                        this.errorReturnBytes = unCompressByGZIP(this.errorReturnBytes);
                    }
                }
            }
        }

    }


    public byte[] unCompressByGZIP(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return bytes;
        }
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
            ReadableByteChannel readableByteChannel = Channels.newChannel(gzipInputStream);
            WritableByteChannel writableByteChannel = Channels.newChannel(byteArrayOutputStream);
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            while (readableByteChannel.read(byteBuffer) != -1) {
                byteBuffer.flip();
                while (byteBuffer.hasRemaining()) {
                    writableByteChannel.write(byteBuffer);
                }
                byteBuffer.clear();
            }
            byte[] newBytes = byteArrayOutputStream.toByteArray();
            writableByteChannel.close();
            readableByteChannel.close();
            byteArrayOutputStream.flush();
            byteArrayOutputStream.close();
            gzipInputStream.close();
            byteArrayInputStream.close();
            return newBytes;
        } catch (Exception e) {
            e.printStackTrace();
            return bytes;
        }
    }


    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public byte[] getReturnBytes() {
        return returnBytes;
    }

    public void setReturnBytes(byte[] returnBytes) {
        this.returnBytes = returnBytes;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    public byte[] getErrorReturnBytes() {
        return errorReturnBytes;
    }

    public void setErrorReturnBytes(byte[] errorReturnBytes) {
        this.errorReturnBytes = errorReturnBytes;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public void setHttpVersion(String httpVersion) {
        this.httpVersion = httpVersion;
    }

    public boolean isKeepAlive() {
        return isKeepAlive;
    }

    public void setKeepAlive(boolean keepAlive) {
        isKeepAlive = keepAlive;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
    }

    public String getCharSet() {
        return charSet;
    }

    public void setCharSet(String charSet) {
        this.charSet = charSet;
    }


    public String getClientCertificateName() {
        return clientCertificateName;
    }

    public void setClientCertificateName(String clientCertificateName) {
        this.clientCertificateName = clientCertificateName;
    }

    public X509Certificate[] getClientX509Certificates() {
        return clientX509Certificates;
    }

    public void setClientX509Certificates(X509Certificate[] clientX509Certificates) {
        this.clientX509Certificates = clientX509Certificates;
    }

    public String getServerCertificateName() {
        return serverCertificateName;
    }

    public void setServerCertificateName(String serverCertificateName) {
        this.serverCertificateName = serverCertificateName;
    }

    public X509Certificate[] getServerX509Certificates() {
        return serverX509Certificates;
    }

    public void setServerX509Certificates(X509Certificate[] serverX509Certificates) {
        this.serverX509Certificates = serverX509Certificates;
    }

    @Override
    public String toString() {
        return getClass().getName() + "@" + Integer.toHexString(hashCode()) + "\r\n状态码：\r\n" + this.getCode() + "\r\ncontentType：\r\n" + this.getContentType() + "\r\n字符：\r\n" + this.getCharSet() + "\r\n内容:\r\n" + this.getResponseBodyToString();
    }
}
