package cn.katoumegumi.java.http.client.model;

public enum MediaType {
    ALL_VALUE("*/*"),
    TEXT_HTML("text/html"),
    TEXT_PLAIN("text/plain"),
    TEXT_XML("text/xml"),
    IMAGE_GIF("image/gif"),
    IMAGE_JPEG("image/jpeg"),
    IMAGE_PNG("image/png"),
    APPLICATION_XML("application/xml"),
    APPLICATION_XHTML("application/xhtml+xml"),
    APPLICATION_ATOM("application/atom+xml"),
    APPLICATION_PDF("application/pdf"),
    APPLICATION_MS_WORD("application/msword"),
    MULTIPART_FORM_DATA("multipart/form-data"),
    APPLICATION_FORM_URLENCODED("application/x-www-form-urlencoded"),
    APPLICATION_JSON("application/json"),
    APPLICATION_OCTET_STREAM("application/octet-stream");


    private String value;

    private MediaType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
